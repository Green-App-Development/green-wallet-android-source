package com.android.greenapp.data.interact

import android.os.Build
import androidx.annotation.RequiresApi
import com.android.greenapp.data.local.TokenDao
import com.android.greenapp.data.local.TransactionDao
import com.android.greenapp.data.local.WalletDao
import com.android.greenapp.data.local.entity.TransactionEntity
import com.android.greenapp.data.local.entity.WalletEntity
import com.android.greenapp.data.network.BlockChainService
import com.android.greenapp.data.network.ExternalService
import com.android.greenapp.data.network.dto.greenapp.network.NetworkItem
import com.android.greenapp.data.preference.PrefsManager
import com.android.greenapp.domain.entity.Wallet
import com.android.greenapp.domain.interact.BlockChainInteract
import com.android.greenapp.domain.interact.PrefsInteract
import com.android.greenapp.presentation.custom.*
import com.android.greenapp.presentation.main.send.spend.Coin
import com.android.greenapp.presentation.main.send.spend.CoinSpend
import com.android.greenapp.presentation.main.send.spend.SpenBunde
import com.android.greenapp.presentation.main.send.spend.SpendBundle
import com.android.greenapp.presentation.tools.BASE_URL_SPACE_SCAN
import com.android.greenapp.presentation.tools.Resource
import com.android.greenapp.presentation.tools.Status
import com.example.common.tools.VLog
import com.google.gson.Gson
import org.json.JSONObject
import retrofit2.Retrofit
import java.util.*
import javax.inject.Inject


/**
 * Created by bekjan on 06.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class BlockChainInteractImpl @Inject constructor(
	private val blockChainService: BlockChainService,
	private val walletDao: WalletDao,
	private val prefs: PrefsInteract,
	private val transactionDao: TransactionDao,
	private val retrofitBuilder: Retrofit.Builder,
	private val gson: Gson,
	private val tokenDao: TokenDao,
	private val encryptor: AESEncryptor,
	private val notificationHelper: NotificationHelper
) :
	BlockChainInteract {

	@RequiresApi(Build.VERSION_CODES.N)
	override suspend fun saveNewWallet(
		wallet: Wallet,
		imported: Boolean
	): Resource<String> {
		val key = encryptor.getAESKey(prefs.getSettingString(PrefsManager.PASSCODE, ""))
		val encMnemonics = encryptor.encrypt(wallet.mnemonics.toString(), key!!)
		val walletEntity = wallet.toWalletEntity(encMnemonics)
		walletDao.insertWallet(walletEntity = walletEntity)
		if (imported) {
			updateWalletBalance(walletEntity)
			updateTokenBalanceWithFullNode(walletEntity)
		}
		return Resource.success("OK")
	}

	private suspend fun getNetworkItemFromPrefs(networkType: String): NetworkItem? {
		val item = prefs.getObjectString(getPreferenceKeyForNetworkItem(networkType))
		if (item.isEmpty()) return null
		return gson.fromJson(item, NetworkItem::class.java)
	}

	private suspend fun getEncryptedMnemonics(mnemonics: List<String>): String {
		kotlin.runCatching {
			val decMnemonicsStr = mnemonics.toString()
			val secretKeySpec =
				encryptor.getAESKey(prefs.getSettingString(PrefsManager.PASSCODE, ""))
			return encryptor.encrypt(decMnemonicsStr, secretKeySpec!!)
		}.onFailure {
			VLog.d("Exception in encrypting mnemonics : ${it.message}")
		}
		return ""
	}

	override suspend fun updateBalanceAndTransactionsPeriodically() {
		val walletListDb = walletDao.getAllWalletList()
		for (wallet in walletListDb) {
			updateWalletBalance(wallet)
			updateTokenBalanceWithFullNode(wallet)
		}
	}

	override suspend fun push_tx(
		jsonSpendBundle: String,
		url: String,
		sendAmount: Double,
		networkType: String,
		fingerPrint: Long,
		code: String
	): Resource<String> {

		try {
			VLog.d("Push method got called in data layer code : $networkType")
			val curBlockChainService =
				retrofitBuilder.baseUrl("$url/").build()
					.create(BlockChainService::class.java)

			val spendBundleJson = JSONObject(jsonSpendBundle)
			val agg_signature = spendBundleJson.getString("aggregated_signature")

			val coinSpends = mutableListOf<CoinSpend>()
			val coinSpendsSize = spendBundleJson.getJSONArray("coin_spends").length()

			for (i in 0 until coinSpendsSize) {
				val coin_spend =
					JSONObject(spendBundleJson.getJSONArray("coin_spends")[i].toString())

				val puzzle_reveal = coin_spend.getString("puzzle_reveal")
				val solution = coin_spend.getString("solution")
				val coinJSON = JSONObject(coin_spend.get("coin").toString())
				val parent_coin_info = coinJSON.getString("parent_coin_info")
				val puzzle_hash = coinJSON.getString("puzzle_hash")
				val amount = coinJSON.getLong("amount")
				val coin = Coin(amount, parent_coin_info, puzzle_hash)
				val coinSpend = CoinSpend(coin, puzzle_reveal, solution)
				coinSpends.add(coinSpend)
			}
			val spendBundle = SpendBundle(agg_signature, coinSpends.toList())
			val spenBundle = SpenBunde(spendBundle)
			VLog.d("SpendBundle Sending to server push_tx : $spenBundle")
			val res = curBlockChainService.pushTransaction(spenBundle)
			VLog.d("Result from push_transaction  ${res.body()} : ${res.body()!!.status}")
			if (res.isSuccessful) {
				val status = res.body()!!.status
				if (status == "SUCCESS") {
					//Temporary  Generate Fake Height
					val trans = TransactionEntity(
						UUID.randomUUID().toString(),
						sendAmount,
						System.currentTimeMillis(),
						temporarilyGenerateFakeHeight(),
						Status.Outgoing,
						networkType,
						"",
						fingerPrint,
						0.0,
						code
					)
					VLog.d("Inserting transaction entity : $trans")
					transactionDao.insertTransaction(trans)
					return Resource.success("OK")
				}
			}
		} catch (ex: Exception) {

			VLog.d("Exception occured in push_tx transaction : ${ex.message}")
			return Resource.error(ex)
		}
		return Resource.error(Exception("Unknown exception in pushing pushing transaction"))
	}

	fun temporarilyGenerateFakeHeight(): Long {
		return (2002200 + Math.random() * (2604400 - 2002200)).toLong()
	}

	suspend fun updateTokenBalanceWithFullNode(wallet: WalletEntity) {
		try {
			if (isThisChivesNetwork(wallet.networkType)) return
			val networkItem = getNetworkItemFromPrefs(wallet.networkType)
				?: throw Exception("Exception in converting json str to networkItem")

			val curBlockChainService =
				retrofitBuilder.baseUrl(networkItem.full_node + "/").build()
					.create(BlockChainService::class.java)
			val hashWithAmount = hashMapOf<String, Double>()
			for ((asset_id, puzzle_hash) in wallet.hashListImported) {
				val body = hashMapOf<String, Any>()
				body["puzzle_hash"] =
					puzzle_hash
				body["include_spent_coins"] = false
				val division = 1000.0
				var balance = 0L
				val prevAmount = wallet.hashWithAmount[asset_id] ?: 0.0
				val request = curBlockChainService.queryBalance(body)
				if (request.isSuccessful) {
					val coinRecordsJsonArray = request.body()!!["coin_records"].asJsonArray
					for (coin in coinRecordsJsonArray) {
						val jsCoin = coin.asJsonObject
						val curAmount =
							jsCoin.get("coin").asJsonObject.get("amount").asLong
						val spent = jsCoin.get("spent").asBoolean
						if (!spent)
							balance += curAmount
					}
				}
				val newAmount = balance / division
				hashWithAmount[asset_id] = newAmount
				if (newAmount != prevAmount) {
					getIncomingTransactionNotifForToken(
						puzzle_hash,
						wallet,
						curBlockChainService,
						asset_id
					)
				}
			}
			if (wallet.hashWithAmount != hashWithAmount) {
				walletDao.updateChiaNetworkHashTokenBalance(wallet.fingerPrint, hashWithAmount)
			}
		} catch (ex: java.lang.Exception) {
			VLog.d("Exception occurred in updating token balance : ${ex.message}")
		}
	}

	private suspend fun getIncomingTransactionNotifForToken(
		puzzleHash: String,
		wallet: WalletEntity,
		curBlockChainService: BlockChainService,
		assetId: String
	) {
		try {
			val body = hashMapOf<String, Any>()
			body["puzzle_hash"] =
				puzzleHash
			body["include_spent_coins"] = false
			val division = 1000.0
			val code = tokenDao.getTokenByHash(assetId).get().code
			val request = curBlockChainService.queryBalance(body)
			if (request.isSuccessful) {
				val coinRecordsJsonArray = request.body()!!["coin_records"].asJsonArray
				for (record in coinRecordsJsonArray) {
					val jsRecord = record.asJsonObject
					val coinAmount =
						jsRecord.get("coin").asJsonObject.get("amount").asLong
					val spent = jsRecord.get("spent").asBoolean
					val timeStamp = jsRecord.get("timestamp").asLong
					val height = jsRecord.get("confirmed_block_index").asLong
					val parent_coin_info =
						jsRecord.get("coin").asJsonObject.get("parent_coin_info").asString
					val parent_puzzle_hash = getParentCoinsSpentAmountAndHashValue(
						parent_coin_info,
						curBlockChainService
					)!!["puzzle_hash"] as String
					val transExistByParentInfo =
						transactionDao.checkTransactionByIDExistInDB(parent_coin_info)
					val timeValidate = timeStamp * 1000 > wallet.savedTime
					val parent_puzzle_hash_match = parent_puzzle_hash.substring(2) != puzzleHash
					if (timeStamp * 1000 > wallet.savedTime && parent_puzzle_hash.substring(2) != puzzleHash && !transExistByParentInfo.isPresent) {
						val transEntity = TransactionEntity(
							parent_coin_info,
							coinAmount / division,
							System.currentTimeMillis(),
							height,
							Status.Incoming,
							wallet.networkType,
							"",
							wallet.fingerPrint,
							0.0,
							code
						)
						val formatted = formattedDoubleAmountWithPrecision(coinAmount / division)
						notificationHelper.callGreenAppNotificationMessages(
							"Incoming Transaction Token : $formatted",
							System.currentTimeMillis()
						)
						VLog.d("Inserting New Incoming Transaction Token  : $transEntity")
						transactionDao.insertTransaction(transEntity)
					} else {
						VLog.d("Current incoming transactions Token is already saved or can't be saved : $jsRecord and parentPuzzle_Hash : $parent_puzzle_hash and parentInfo exist : $transExistByParentInfo")
						VLog.d("Validate time for transactions Token : $timeValidate , match parent puzzle hash match : $parent_puzzle_hash_match")
					}
				}

			} else {
				VLog.d("Request is not success in updating incoming transactions : ${request.message()}")
			}
		} catch (ex: Exception) {
			VLog.d("Exception  occurred in updating incoming transaction for notification :  ${ex.message}")
		}
	}

	suspend fun updateWalletBalance(wallet: WalletEntity) {
		try {
			val networkItem = getNetworkItemFromPrefs(wallet.networkType)
				?: throw Exception("Exception in converting json str to networkItem")

			val curBlockChainService =
				retrofitBuilder.baseUrl(networkItem.full_node + "/").build()
					.create(BlockChainService::class.java)
			val body = hashMapOf<String, Any>()
			body["puzzle_hash"] =
				wallet.sk
			body["include_spent_coins"] = false
			val division =
				if (isThisChivesNetwork(wallet.networkType)) Math.pow(
					10.0,
					8.0
				) else Math.pow(
					10.0,
					12.0
				)
			var balance = 0L
			val request = curBlockChainService.queryBalance(body)
			if (request.isSuccessful) {
				val coinRecordsJsonArray = request.body()!!["coin_records"].asJsonArray
				for (coin in coinRecordsJsonArray) {
					val jsCoin = coin.asJsonObject
					val curAmount =
						jsCoin.get("coin").asJsonObject.get("amount").asLong
					val spent = jsCoin.get("spent").asBoolean
					if (!spent)
						balance += curAmount
				}
				val prevBalance =
					walletDao.getWalletByFingerPrint(wallet.fingerPrint).get(0).balance
				val newBalance = balance / division
				VLog.d("Updating balance for wallet : ${wallet.fingerPrint} from $prevBalance to balance : $newBalance")
				if (prevBalance != newBalance) {
					VLog.d("CurBalance is not the same as the previous one for wallet : ${wallet.fingerPrint}")
					walletDao.updateWalletBalance(newBalance, wallet.fingerPrint)
					updateIncomingTransactions(wallet)
				}
			} else {
				VLog.d("Request is not success in updating balance : ${request.message()}")
			}
		} catch (ex: Exception) {
			VLog.d("Exception occurred in updating wallet balance : ${ex.message}")
		}
	}

	suspend fun updateIncomingTransactions(wallet: WalletEntity) {
		try {
			val networkItem = getNetworkItemFromPrefs(wallet.networkType)
				?: throw Exception("Exception in converting json str to networkItem")

			val curBlockChainService =
				retrofitBuilder.baseUrl(networkItem.full_node + "/").build()
					.create(BlockChainService::class.java)
			val body = hashMapOf<String, Any>()
			body["puzzle_hash"] =
				wallet.sk
			body["include_spent_coins"] = false
			val division =
				if (isThisChivesNetwork(wallet.networkType)) Math.pow(
					10.0,
					8.0
				) else Math.pow(
					10.0,
					12.0
				)
			val request = curBlockChainService.queryBalance(body)
			if (request.isSuccessful) {
				val coinRecordsJsonArray = request.body()!!["coin_records"].asJsonArray
				for (record in coinRecordsJsonArray) {
					val jsRecord = record.asJsonObject
					val coinAmount =
						jsRecord.get("coin").asJsonObject.get("amount").asLong
					val spent = jsRecord.get("spent").asBoolean
					val timeStamp = jsRecord.get("timestamp").asLong
					val height = jsRecord.get("confirmed_block_index").asLong
					val parent_coin_info =
						jsRecord.get("coin").asJsonObject.get("parent_coin_info").asString
					val parent_puzzle_hash = getParentCoinsSpentAmountAndHashValue(
						parent_coin_info,
						curBlockChainService
					)!!["puzzle_hash"] as String
					val transExistByParentInfo =
						transactionDao.checkTransactionByIDExistInDB(parent_coin_info)
					val timeValidate = timeStamp * 1000 > wallet.savedTime
					val parent_puzzle_hash_match = parent_puzzle_hash.substring(2) != wallet.sk
					if (timeStamp * 1000 > wallet.savedTime && parent_puzzle_hash.substring(2) != wallet.sk && !transExistByParentInfo.isPresent) {
						val transEntity = TransactionEntity(
							parent_coin_info,
							coinAmount / division,
							System.currentTimeMillis(),
							height,
							Status.Incoming,
							wallet.networkType,
							"",
							wallet.fingerPrint,
							0.0,
							getShortNetworkType(wallet.networkType)
						)
						val formatted = formattedDoubleAmountWithPrecision(coinAmount / division)
						notificationHelper.callGreenAppNotificationMessages(
							"Incoming Transaction : $formatted",
							System.currentTimeMillis()
						)
						VLog.d("Inserting New Incoming Transaction  : $transEntity")
						transactionDao.insertTransaction(transEntity)
					} else {
						VLog.d("Current incoming transactions is already saved or can't be saved : $jsRecord and parentPuzzle_Hash : $parent_puzzle_hash and parentInfo exist : $transExistByParentInfo")
						VLog.d("Validate time for transactions : $timeValidate , match parent puzzle hash match : $parent_puzzle_hash_match")
					}
				}

			} else {
				VLog.d("Request is not success in updating incoming transactions : ${request.message()}")
			}
		} catch (ex: Exception) {
			VLog.d("Exception caught in updating transaction : ${ex.message}")
		}
	}

	suspend fun getParentCoinsSpentAmountAndHashValue(
		parent_info: String,
		service: BlockChainService
	): HashMap<Any, Any>? {
		val res = hashMapOf<Any, Any>()
		val body = hashMapOf<String, Any>()
		body["name"] = parent_info
		body["include_spent_coins"] = true
		val request = service.getCoinRecordByName(body)
		if (request.isSuccessful) {
			val coinRecordJson = request.body()!!["coin_record"].asJsonObject
			val amount = coinRecordJson.get("coin").asJsonObject.get("amount").asLong
			val puzzle_hash = coinRecordJson.get("coin").asJsonObject.get("puzzle_hash").asString
			res["amount"] = amount
			res["puzzle_hash"] = puzzle_hash
			return res
		} else {
			VLog.d("Request in getting parent coin info is not success : ${request.body()}")
		}
		return null
	}


	suspend fun updateTransactionsBalance(
		wallet: WalletEntity,
		isNewWallet: Boolean,
		includeSpentCoins: Boolean
	) {
		try {
			val networkItem = getNetworkItemFromPrefs(wallet.networkType)
				?: throw Exception("Exception in converting json str to networkItem")

			val curBlockChainService =
				retrofitBuilder.baseUrl(networkItem.full_node + "/").build()
					.create(BlockChainService::class.java)
			val body = hashMapOf<String, Any>()
			body["puzzle_hash"] =
				wallet.sk
			body["include_spent_coins"] = includeSpentCoins
			val division =
				if (isThisChivesNetwork(wallet.networkType)) Math.pow(
					10.0,
					8.0
				) else Math.pow(
					10.0,
					12.0
				)
			var balance = 0L
			val allTrans = mutableListOf<TransactionEntity>()
			val request = curBlockChainService.queryBalance(body)
			if (request.isSuccessful) {
				val coinRecordsJsonArray = request.body()!!["coin_records"].asJsonArray
				for (coin in coinRecordsJsonArray) {
					val jsCoin = coin.asJsonObject
					val curAmount =
						jsCoin.get("coin").asJsonObject.get("amount").asLong
					val confirmed_block_index = jsCoin.get("confirmed_block_index").asLong
					val timeStamp = jsCoin.get("timestamp").asLong * 1000
					val parent_coin_info =
						jsCoin.get("coin").asJsonObject.get("parent_coin_info").asString
					val spent = jsCoin.get("spent").asBoolean
					if (!spent)
						balance += curAmount

					var status = Status.Outgoing
				}
				for (tran in allTrans) {
					val tranExist =
						transactionDao.checkTransactionByIDExistInDB(tran.transaction_id)
					if (!tranExist.isPresent && !isNewWallet && tran.status == Status.Incoming) {
						notificationHelper.callGreenAppNotificationMessages(
							"You received new transaction",
							System.currentTimeMillis()
						)
					}
					VLog.d("Inserting transaction into : transDAO : $tran")
					transactionDao.insertTransaction(tran)
				}
				val prevBalance =
					walletDao.getWalletByFingerPrint(wallet.fingerPrint).get(0).balance
				val newBalance = balance / division
				VLog.d("Updating balance for wallet : ${wallet.fingerPrint} from $prevBalance to balance : $newBalance")
				if (prevBalance != newBalance)
					walletDao.updateWalletBalance(newBalance, wallet.fingerPrint)
			} else {
				VLog.d("Request is not success in updating transactions : ${request.body()}")
			}
		} catch (ex: Exception) {
			VLog.d("Exception in updating transactions only  : ${ex.message}")
		}
	}

}
