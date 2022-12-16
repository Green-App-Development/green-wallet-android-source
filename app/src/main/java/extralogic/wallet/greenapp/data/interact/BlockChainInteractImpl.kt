package extralogic.wallet.greenapp.data.interact

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.common.tools.VLog
import com.example.common.tools.getTokenPrecisionByCode
import com.google.gson.Gson
import extralogic.wallet.greenapp.data.local.*
import extralogic.wallet.greenapp.data.local.entity.TransactionEntity
import extralogic.wallet.greenapp.data.local.entity.WalletEntity
import extralogic.wallet.greenapp.data.managers.OutgoingTransactionManager
import extralogic.wallet.greenapp.data.network.BlockChainService
import extralogic.wallet.greenapp.data.network.dto.greenapp.network.NetworkItem
import extralogic.wallet.greenapp.data.network.dto.spendbundle.CoinDto
import extralogic.wallet.greenapp.data.network.dto.spendbundle.CoinSpend
import extralogic.wallet.greenapp.data.network.dto.spendbundle.SpenBunde
import extralogic.wallet.greenapp.data.network.dto.spendbundle.SpendBundle
import extralogic.wallet.greenapp.data.preference.PrefsManager
import extralogic.wallet.greenapp.domain.domainmodel.Wallet
import extralogic.wallet.greenapp.domain.interact.BlockChainInteract
import extralogic.wallet.greenapp.domain.interact.GreenAppInteract
import extralogic.wallet.greenapp.domain.interact.PrefsInteract
import extralogic.wallet.greenapp.domain.interact.SpentCoinsInteract
import extralogic.wallet.greenapp.presentation.custom.*
import extralogic.wallet.greenapp.presentation.tools.Resource
import extralogic.wallet.greenapp.presentation.tools.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Retrofit
import java.util.*
import javax.inject.Inject


/**
 * Created by bekjan on 06.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class BlockChainInteractImpl @Inject constructor(
	private val walletDao: WalletDao,
	private val prefs: PrefsInteract,
	private val transactionDao: TransactionDao,
	private val retrofitBuilder: Retrofit.Builder,
	private val gson: Gson,
	private val tokenDao: TokenDao,
	private val encryptor: AESEncryptor,
	private val notificationHelper: NotificationHelper,
	private val spentCoinsInteract: SpentCoinsInteract,
	private val spentCoinsDao: SpentCoinsDao,
	private val greenAppInteract: GreenAppInteract,
	private val context: Context,
	private val outgoingTransactionManager: OutgoingTransactionManager
) :
	BlockChainInteract {

	private val mutexUpdateBalance = Mutex()

	@RequiresApi(Build.VERSION_CODES.N)
	override suspend fun saveNewWallet(
		wallet: Wallet,
		imported: Boolean
	): Resource<String> {
		val key = encryptor.getAESKey(prefs.getSettingString(PrefsManager.PASSCODE, ""))
		val encMnemonics = encryptor.encrypt(wallet.mnemonics.toString(), key!!)
		var savedTime = greenAppInteract.getServerTime()
		if (savedTime == -1L)
			savedTime = System.currentTimeMillis()
		val walletEntity = wallet.toWalletEntity(encMnemonics, savedTime)
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

	override suspend fun updateBalanceAndTransactionsPeriodically() {
		mutexUpdateBalance.withLock {
			val walletListDb = walletDao.getAllWalletList()
			for (wallet in walletListDb) {
				updateInProgressTransactions()
				updateWalletBalance(wallet)
				updateTokenBalanceWithFullNode(wallet)
			}
		}
	}

	private suspend fun updateInProgressTransactions() {
		try {
			val inProgressTrans = transactionDao.getTransactionsByStatus(Status.InProgress)
			for (tran in inProgressTrans) {
				val unSpentCoinRecordHeight = getFirstUnSpentCoinRecordHeight(tran)
				VLog.d("Trying to update coin Record Height for inProgress transaction : $tran : $unSpentCoinRecordHeight")
				if (unSpentCoinRecordHeight != -1L) {
					transactionDao.updateTransactionStatusHeight(
						Status.Outgoing,
						unSpentCoinRecordHeight,
						tran.transaction_id
					)
					val deleteSpentCoinsRow =
						spentCoinsDao.deleteSpentConsByTimeCreated(tran.created_at_time)
					VLog.d("Affected rows when deleting spentCoins : $deleteSpentCoinsRow")
					val formatted = formattedDoubleAmountWithPrecision(tran.amount)
					val resLanguageResource =
						prefs.getSettingString(PrefsManager.LANGUAGE_RESOURCE, "")
					val resMap = Converters.stringToHashMap(resLanguageResource)
					val outgoing_transaction =
						resMap["push_notifications_outgoing"]
							?: "Outgoing transaction"
					notificationHelper.callGreenAppNotificationMessages(
						"$outgoing_transaction : $formatted ${tran.code}",
						System.currentTimeMillis()
					)
				}
			}
		} catch (ex: java.lang.Exception) {
			VLog.d("Exception  occurred in updating InProgress Transaction : ${ex.message}")
		}
	}

	private suspend fun getFirstUnSpentCoinRecordHeight(tran: TransactionEntity): Long {
		try {
			val networkItem = getNetworkItemFromPrefs(tran.networkType)
				?: throw Exception("Exception in converting json str to networkItem")
			val curBlockChainService =
				retrofitBuilder.baseUrl(networkItem.full_node + "/").build()
					.create(BlockChainService::class.java)
			val body = hashMapOf<String, Any>()
			body["puzzle_hash"] =
				tran.to_dest_hash
			body["include_spent_coins"] = false
			val division = getTokenPrecisionByCode(tran.code)
			val request = curBlockChainService.queryBalanceWithSorting(body)
			if (request.isSuccessful) {
				val coinRecordsIncreasing =
					request.body()!!.coin_records.sortedWith { p0, p1 -> if (p0.timestamp >= p1.timestamp) 1 else -1 }
//				VLog.d("CoinRecordsIncreasing to confirm incoming transaction : $coinRecordsIncreasing")
				for (record in coinRecordsIncreasing) {
					val coinAmount =
						record.coin.amount / division
					val timeStamp = record.timestamp
//					VLog.d("TimeStamp of updating trans : timeStamp : ${timeStamp} trantime : ${tran.created_at_time}, CoinAmount : $coinAmount , TranCoinAmount : ${tran.amount}")
					if (coinAmount == tran.amount && timeStamp >=
						tran.created_at_time / 1000 - 120
					) {
						return record.confirmed_block_index
					}
				}
			}
		} catch (ex: java.lang.Exception) {
			VLog.d("Exception in getting unspent transactions coin records :  $ex")
		}
		return -1
	}


	override suspend fun push_tx(
		jsonSpendBundle: String,
		url: String,
		sendAmount: Double,
		networkType: String,
		fingerPrint: Long,
		code: String,
		dest_puzzle_hash: String,
		address: String,
		fee: Double,
		spentCoinsJson: String,
		spentCoinsToken: String
	): Resource<String> {
		try {
			val serverTime = greenAppInteract.getServerTime()
			if (serverTime == -1L)
				throw  ServerMaintenanceExceptions()
			val timeBeforePushingTrans = serverTime - 60 * 1000
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
				val parent_coin_info = "0x" + coinJSON.getString("parent_coin_info")
				val puzzle_hash = "0x" + coinJSON.getString("puzzle_hash")
				val amount = coinJSON.getLong("amount")
				val coin = CoinDto(amount, parent_coin_info, puzzle_hash)
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
					val trans = TransactionEntity(
						UUID.randomUUID().toString(),
						sendAmount,
						timeBeforePushingTrans,
						0,
						Status.InProgress,
						networkType,
						dest_puzzle_hash,
						address,
						fee,
						code
					)
					VLog.d("Inserting transaction entity : $trans and coinJson $spentCoinsJson and coinToken : $spentCoinsToken")
					transactionDao.insertTransaction(trans)
					spentCoinsInteract.insertSpentCoinsJson(
						spentCoinsJson,
						timeBeforePushingTrans,
						getShortNetworkType(networkType),
						address
					)
					spentCoinsInteract.insertSpentCoinsJson(
						spentCoinsToken,
						timeBeforePushingTrans,
						code,
						address
					)
					return Resource.success("OK")
				}
			} else {
				withContext(Dispatchers.Main) {
					Toast.makeText(context, "Error from server: ${res.body()}", Toast.LENGTH_LONG)
						.show()
				}
			}
		} catch (ex: Exception) {
			withContext(Dispatchers.Main) {
				Toast.makeText(context, "Error in try catch: ${ex.message}", Toast.LENGTH_LONG)
					.show()
			}
			VLog.d("Exception occured in push_tx transaction : ${ex.message}")
			return Resource.error(ex)
		}
		return Resource.error(Exception("Unknown exception in pushing pushing transaction"))
	}

	override suspend fun updateTokenBalanceWithFullNode(wallet: WalletEntity) {
		try {
			if (isThisChivesNetwork(wallet.networkType)) return
			val networkItem = getNetworkItemFromPrefs(wallet.networkType)
				?: throw Exception("Exception in converting json str to networkItem")

			val curBlockChainService =
				retrofitBuilder.baseUrl(networkItem.full_node + "/").build()
					.create(BlockChainService::class.java)
			val hashWithAmount = hashMapOf<String, Double>()
			for ((asset_id, puzzle_hashes) in wallet.hashListImported) {
				val body = hashMapOf<String, Any>()
				body["puzzle_hashes"] =
					puzzle_hashes
				body["include_spent_coins"] = false
				val division = 1000.0
				var balance = 0L
				val prevAmount = wallet.hashWithAmount[asset_id] ?: 0.0
				val request = curBlockChainService.queryBalanceByPuzzleHashes(body)
				val tokeEntity = tokenDao.getTokenByHash(hash = asset_id).get()
				val prevTokenStartHeight = wallet.tokensStartHeight
				val prevStartHeight = prevTokenStartHeight[tokeEntity.code] ?: 0
				var newStartHeight = Long.MAX_VALUE
				if (request.isSuccessful) {
					val coinRecordsJsonArray = request.body()!!.coin_records
					for (coin in coinRecordsJsonArray) {
						val curAmount = coin.coin.amount
						newStartHeight = Math.min(newStartHeight, coin.confirmed_block_index)
						val spent = coin.spent
						if (!spent)
							balance += curAmount
					}
				}
				val newAmount = balance / division
				hashWithAmount[asset_id] = newAmount
				if (newAmount != prevAmount) {
					prevTokenStartHeight[tokeEntity.code] = newStartHeight
					walletDao.updateTokenStartHeightByAddress(prevTokenStartHeight, wallet.address)
					getIncomingTransactionNotifForToken(
						puzzle_hashes,
						wallet,
						curBlockChainService,
						asset_id
					)
//					outgoingTransactionManager.checkOutGoingTransactions(puzzle_hashes,prevStartHeight,wallet,curBlockChainService,
//						getTokenPrecisionByCode(tokeEntity.code), tokenCode = tokeEntity.code)
				}
			}
			if (wallet.hashWithAmount != hashWithAmount) {
				walletDao.updateChiaNetworkHashTokenBalanceByAddress(wallet.address, hashWithAmount)
			}
		} catch (ex: java.lang.Exception) {
			VLog.d("Exception occurred in updating token balance : ${ex.message}")
		}
	}

	private suspend fun getIncomingTransactionNotifForToken(
		puzzleHashes: List<String>,
		wallet: WalletEntity,
		curBlockChainService: BlockChainService,
		assetId: String
	) {
		try {
			val body = hashMapOf<String, Any>()
			body["puzzle_hashes"] =
				puzzleHashes
			body["include_spent_coins"] = false
			val division = 1000.0
			val code = tokenDao.getTokenByHash(assetId).get().code
			val request = curBlockChainService.queryBalanceByPuzzleHashes(body)
			if (request.isSuccessful) {
				val coinRecordsJsonArray = request.body()!!.coin_records
				for (record in coinRecordsJsonArray) {
					val coinAmount = record.coin.amount
					val timeStamp = record.timestamp
					val height = record.confirmed_block_index
					val parent_coin_info = record.coin.parent_coin_info
					val parent_puzzle_hash = getParentCoinsSpentAmountAndHashValue(
						parent_coin_info,
						curBlockChainService
					)!!["puzzle_hash"] as String
					val transExistByParentInfo =
						transactionDao.checkTransactionByIDExistInDB(parent_coin_info)
					if (timeStamp * 1000 >= wallet.savedTime && !puzzleHashes.contains(
							parent_puzzle_hash.substring(
								2
							)
						) && !transExistByParentInfo.isPresent
					) {
//						VLog.d("Actual timeStamp of incoming coin record : $timeStamp and walletCreatedTime : ${wallet.savedTime}")
						val transEntity = TransactionEntity(
							parent_coin_info,
							coinAmount / division,
							greenAppInteract.getServerTime() - 60 * 1000,
							height,
							Status.Incoming,
							wallet.networkType,
							"",
							wallet.address,
							0.0,
							code
						)
						val formatted = formattedDoubleAmountWithPrecision(coinAmount / division)
						val tokenCode = tokenDao.getTokenByHash(assetId).get().code
						val resLanguageResource =
							prefs.getSettingString(PrefsManager.LANGUAGE_RESOURCE, "")
						val resMap = Converters.stringToHashMap(resLanguageResource)
						val incoming_transaction =
							resMap["push_notifications_incoming"]
								?: "Incoming transaction"
						notificationHelper.callGreenAppNotificationMessages(
							"$incoming_transaction : $formatted $tokenCode",
							System.currentTimeMillis()
						)
						VLog.d("Inserting New Incoming Transaction Token  : $transEntity")
						transactionDao.insertTransaction(transEntity)
					} else {
						VLog.d("Current incoming transactions Token is already saved or can't be saved : $record and parentPuzzle_Hash : $parent_puzzle_hash and parentInfo exist : $transExistByParentInfo")
//						VLog.d("Validate time for transactions Token : $timeValidate , match parent puzzle hash match : $parent_puzzle_hash_match")
					}
				}

			} else {
				VLog.d("Request is not success in updating incoming transactions : ${request.message()}")
			}
		} catch (ex: Exception) {
			VLog.d("Exception  occurred in updating incoming transaction for notification :  ${ex.message}")
		}
	}

	override suspend fun updateWalletBalance(wallet: WalletEntity) {
		try {
			val networkItem = getNetworkItemFromPrefs(wallet.networkType)
				?: throw Exception("Exception in converting json str to networkItem")

			val curBlockChainService =
				retrofitBuilder.baseUrl(networkItem.full_node + "/").build()
					.create(BlockChainService::class.java)
			val tokensStartHeight = wallet.tokensStartHeight
			val prevStartHeight = tokensStartHeight[getShortNetworkType(wallet.networkType)] ?: 0
			val body = hashMapOf<String, Any>()
			body["puzzle_hashes"] =
				wallet.puzzle_hashes
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
			val request = curBlockChainService.queryBalanceByPuzzleHashes(body)
			var newStartHeight = Long.MAX_VALUE
			if (request.isSuccessful) {
				for (coin in request.body()!!.coin_records) {
					val curAmount = coin.coin.amount
					val spent = coin.spent
					newStartHeight = Math.min(newStartHeight, coin.confirmed_block_index)
					if (!spent)
						balance += curAmount
				}
				val prevBalance =
					walletDao.getWalletByAddress(wallet.address).get(0).balance
				val newBalance = balance / division
				VLog.d("Updating balance for wallet : ${wallet.fingerPrint} from $prevBalance to balance : $newBalance and newStartHeight : $newStartHeight")
				if (prevBalance != newBalance) {
					VLog.d("CurBalance is not the same as the previous one for wallet : ${wallet.fingerPrint}")
					tokensStartHeight[getShortNetworkType(wallet.networkType)] =
						newStartHeight
					walletDao.updateTokenStartHeightByAddress(tokensStartHeight, wallet.address)
					walletDao.updateWalletBalanceByAddress(newBalance, wallet.address)
					updateIncomingTransactions(wallet)
//					outgoingTransactionManager.checkOutGoingTransactions(wallet.puzzle_hashes,
//						prevStartHeight, wallet, curBlockChainService, division,
//						getShortNetworkType(wallet.networkType)
//					)
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
			body["puzzle_hashes"] =
				wallet.puzzle_hashes
			body["include_spent_coins"] = false
			val division =
				if (isThisChivesNetwork(wallet.networkType)) Math.pow(
					10.0,
					8.0
				) else Math.pow(
					10.0,
					12.0
				)
			val request = curBlockChainService.queryBalanceByPuzzleHashes(body)
			if (request.isSuccessful) {
				val coinRecordsJsonArray = request.body()!!.coin_records
				for (record in coinRecordsJsonArray) {
					val coinAmount = record.coin.amount
					val timeStamp = record.timestamp
					val height = record.confirmed_block_index
					val parent_coin_info = record.coin.parent_coin_info
					val parent_puzzle_hash = getParentCoinsSpentAmountAndHashValue(
						parent_coin_info,
						curBlockChainService
					)!!["puzzle_hash"] as String
					val transExistByParentInfo =
						transactionDao.checkTransactionByIDExistInDB(parent_coin_info)
					if (timeStamp * 1000 >= wallet.savedTime && !wallet.puzzle_hashes.contains(
							parent_puzzle_hash.substring(
								2
							)
						) && !transExistByParentInfo.isPresent
					) {
						VLog.d("Actual timeStamp of incoming coin record : $timeStamp and walletCreatedTime : ${wallet.savedTime}")
						val transEntity = TransactionEntity(
							parent_coin_info,
							coinAmount / division,
							timeStamp * 1000L,
							height,
							Status.Incoming,
							wallet.networkType,
							"",
							wallet.address,
							0.0,
							getShortNetworkType(wallet.networkType)
						)
						val formatted = formattedDoubleAmountWithPrecision(coinAmount / division)
						val resLanguageResource =
							prefs.getSettingString(PrefsManager.LANGUAGE_RESOURCE, "")
						if (resLanguageResource.isEmpty()) return
						val resMap = Converters.stringToHashMap(resLanguageResource)
						val incoming_transaction =
							resMap["push_notifications_incoming"]
								?: "Incoming transaction"
						notificationHelper.callGreenAppNotificationMessages(
							"$incoming_transaction : $formatted ${getShortNetworkType(wallet.networkType)}",
							System.currentTimeMillis()
						)
						VLog.d("Inserting New Incoming Transaction  : $transEntity")
						transactionDao.insertTransaction(transEntity)
					} else {
//						VLog.d("Current incoming transactions is already saved or can't be saved : $record and parentPuzzle_Hash : $parent_puzzle_hash and parentInfo exist : $transExistByParentInfo")
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


}
