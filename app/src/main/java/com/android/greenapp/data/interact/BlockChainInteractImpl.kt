package com.android.greenapp.data.interact

import android.os.Build
import androidx.annotation.RequiresApi
import com.android.greenapp.data.local.TokenDao
import com.android.greenapp.data.local.TransactionDao
import com.android.greenapp.data.local.WalletDao
import com.android.greenapp.data.local.entity.TransactionEntity
import com.android.greenapp.data.local.entity.WalletEntity
import com.android.greenapp.data.network.BlockChainService
import com.android.greenapp.data.network.dto.blockchain.MnemonicDto
import com.android.greenapp.data.network.dto.blockchain.PrivateKeyDto
import com.android.greenapp.data.network.dto.greenapp.network.NetworkItem
import com.android.greenapp.data.preference.PrefsManager
import com.android.greenapp.domain.entity.Wallet
import com.android.greenapp.domain.interact.BlockChainInteract
import com.android.greenapp.domain.interact.PrefsInteract
import com.android.greenapp.presentation.custom.AESEncryptor
import com.android.greenapp.presentation.custom.NotificationHelper
import com.android.greenapp.presentation.custom.getPreferenceKeyForNetworkItem
import com.android.greenapp.presentation.custom.isThisNotChiaNetwork
import com.android.greenapp.presentation.main.send.spend.Coin
import com.android.greenapp.presentation.main.send.spend.CoinSpend
import com.android.greenapp.presentation.main.send.spend.SpenBunde
import com.android.greenapp.presentation.main.send.spend.SpendBundle
import com.android.greenapp.presentation.tools.Resource
import com.android.greenapp.presentation.tools.Status
import com.example.common.tools.VLog
import com.google.gson.Gson
import kotlinx.coroutines.coroutineScope
import org.json.JSONObject
import retrofit2.Retrofit
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

	override suspend fun getGenerateMnemonics(): Resource<MnemonicDto> {
		val res = blockChainService.getGeneratedMnemonics()
		if (res.isSuccessful) {
			val mnemonicsDto = res.body()
			VLog.d("Mnemonic Dto : $mnemonicsDto")
			if (mnemonicsDto?.success == true) {
				return Resource.success(mnemonicsDto)
			} else
				return Resource.error(null)
		}
		return Resource.error(Exception(res.message()))
	}

	@RequiresApi(Build.VERSION_CODES.N)
	override suspend fun saveNewWallet(
		wallet: Wallet
	) {
		val key = encryptor.getAESKey(prefs.getSettingString(PrefsManager.PASSCODE, ""))
		val encMnemonics = encryptor.encrypt(wallet.mnemonics.toString(), key!!)
		val walletEntity = wallet.toWalletEntity(encMnemonics)
		walletDao.insertWallet(walletEntity = walletEntity)
		updateBalanceAndTransaction(walletEntity, true)
	}


	private suspend fun getNetworkItemFromPrefs(networkType: String): NetworkItem? {
		val item = prefs.getObjectString(getPreferenceKeyForNetworkItem(networkType))
		if (item.isEmpty()) return null
		return gson.fromJson(item, NetworkItem::class.java)
	}

	override suspend fun getAllPublicKeys(): Resource<List<Long>> {
		try {
			val res = blockChainService.getAllPublicKeys()
			if (res.isSuccessful) {
				val body = res.body()
				if (body != null) {
					if (body.success) {
						VLog.d("Getting PublicKey : ${body.public_key_fingerprints}")
						val allPubicKeys = body.public_key_fingerprints!!
						return Resource.success(allPubicKeys)
					}
				}
			}
		} catch (ex: java.lang.Exception) {
			VLog.d("Caught exception in getting private key : ${ex.message}")
			return Resource.error(Exception(ex.message))
		}
		return Resource.error(Exception("UnknownException"))
	}

	@RequiresApi(Build.VERSION_CODES.N)
	override suspend fun requestBalanceEachWallets() {
		try {
			val walletList = walletDao.getAllWalletList()
			for (wallet in walletList) {

				val networkItem = getNetworkItemFromPrefs(wallet.networkType)
					?: throw Exception("Exception in getting networkItem")

				val curBlockChainService = retrofitBuilder.baseUrl(networkItem.wallet + "/").build()
					.create(BlockChainService::class.java)
				VLog.d("Requesting balance for fingerPrint : ${wallet.fingerPrint}")
				if (isThisNotChiaNetwork(wallet.networkType)) {
					requestWalletBalance(
						wallet.fingerPrint,
						curBlockChainService,
						wallet.networkType
					)
				} else {
					requestBalanceForChiaNetworkWallet(wallet.fingerPrint, wallet.networkType)
				}
			}
		} catch (ex: Exception) {
			VLog.d("Exception is requestingBalanceEachWallet  : ${ex.message}")
		}
	}

	override suspend fun requestAllTransactions() {
		try {
			val walletList = walletDao.getAllWalletList()
			for (wallet in walletList) {
				requestTransactions(wallet.fingerPrint, wallet.networkType)
			}
		} catch (ex: java.lang.Exception) {
			VLog.d("Exception has been thrown in requesting all transactions : $ex")
		}
	}

	private suspend fun requestTransactions(fingerPrint: Long, networkType: String) {

		val networkItem = getNetworkItemFromPrefs(networkType)
			?: throw Exception("Exception in getting networkItem")

		val curBlockChainService = retrofitBuilder.baseUrl(networkItem.wallet + "/").build()
			.create(BlockChainService::class.java)

		val statusLogIn = logIn(fingerPrint, curBlockChainService)
		if (statusLogIn.state == Resource.State.SUCCESS) {
			val obj = hashMapOf<String, Any>()
			obj["wallet_id"] = 1
			val res = curBlockChainService.getTransactions(obj)
			if (res.isSuccessful && res.body() != null) {

				val transList = res.body()!!.transactions
				if (transList != null) {
					for (tran in transList) {
						var status =
							if (tran.type == 0) Status.Incoming else Status.Outgoing
						if (!tran.confirmed)
							status = Status.InProgress
						val division =
							if (isThisNotChiaNetwork(networkType)) Math.pow(
								10.0,
								8.0
							) else Math.pow(
								10.0,
								12.0
							)
						val balance = tran.amount / division
						transactionDao.insertTransaction(
							tran.toTransactionEntity(
								fingerPrint,
								status,
								networkType,
								balance,
								tran.created_at_time * 1000L
							)
						)
					}
				}
			} else {
				VLog.d("Base Response  is not success")
			}
		}
	}

	private suspend fun requestTransactionsWithoutLogIn(
		fingerPrint: Long,
		curBlockChainService: BlockChainService,
		networkType: String
	) {

		val obj = hashMapOf<String, Any>()
		obj["wallet_id"] = 1
		val res = curBlockChainService.getTransactions(obj)
		if (res.isSuccessful && res.body() != null) {

			val transList = res.body()!!.transactions
			if (transList != null) {
				for (tran in transList) {
					var status =
						if (tran.type == 0) Status.Incoming else Status.Outgoing
					if (!tran.confirmed)
						status = Status.InProgress
					val division =
						if (isThisNotChiaNetwork(networkType)) Math.pow(10.0, 8.0) else Math.pow(
							10.0,
							12.0
						)
					val balance = tran.amount / division
					val transEntity = tran.toTransactionEntity(
						fingerPrint,
						status,
						networkType,
						balance,
						tran.created_at_time * 1000L
					)
					VLog.d("Update transactions : fingerprint : $fingerPrint : Transaction Entity -> $transEntity")
					transactionDao.insertTransaction(
						transEntity
					)
				}
			}
		}
	}


	override suspend fun sendTransaction(
		amount: Double,
		fee: Double,
		address: String,
		fingerPrint: Long,
		networkType: String,
		walletId: Int
	): Resource<String> {
		try {

			val networkItem = getNetworkItemFromPrefs(networkType)
				?: throw Exception("Exception in converting json str to networkItem")

			val curBlockChainService = retrofitBuilder.baseUrl(networkItem.wallet + "/").build()
				.create(BlockChainService::class.java)

			val logInStatus = logIn(fingerPrint, curBlockChainService)
			if (logInStatus.state == Resource.State.SUCCESS) {

				val body = hashMapOf<String, Any>()
				val multiply = if (isThisNotChiaNetwork(networkType)) Math.pow(
					10.0,
					8.0
				) else Math.pow(
					10.0,
					12.0
				)
				body["wallet_id"] = walletId
				body["amount"] = (amount * multiply).toInt()
				body["fee"] = (fee * multiply).toInt()
				body["address"] = address

				VLog.d("LoginStatus is successful for sending transaction : $fingerPrint : $amount and body for sending : $body")

				val res = curBlockChainService.sendTransaction(body)
				VLog.d("JsonObject from sending transaction : ${res.body()}")
				if (res.isSuccessful) {
					if (res.body()!!.success) {
						val sendTransRes = res.body()!!.sendTransResponse!!
						VLog.d("SendTransResponse from send transaction : $sendTransRes")
						val transEntity =
							sendTransRes.toTransactionEntity(
								address,
								fingerPrint,
								networkType,
								sendTransRes.created_at_time * 1000,
								amount
							)
						transactionDao.insertTransaction(transEntity)
//                        requestTransactions(fingerPrint, networkType)
						VLog.d("Success in sending transaction and inserting it to db : $transEntity")
						return Resource.success("OK")
					} else {
						VLog.d("Transaction is not successful  : ${res.message()}")
						return Resource.error(Throwable("Body is not successful"))
					}
				} else {
					VLog.d("Request is not success : $res")
					return Resource.error(Throwable("Error sending tansaction  : ${res}"))
				}
			} else {
				VLog.d("Log in status is not success : ${logInStatus.error}")
				return Resource.error(Throwable("Login in status is not success"))
			}
		} catch (ex: java.lang.Exception) {
			VLog.d("Caught Exception in sending transaction : ${ex.message}")
			return Resource.error(ex)
		}
	}


	@RequiresApi(Build.VERSION_CODES.N)
	suspend fun requestWalletBalance(
		fingerPrint: Long,
		curBlockChainService: BlockChainService,
		networkType: String
	) {
		try {
			val loginStatus = logIn(fingerPrint, curBlockChainService)
			if (loginStatus.state == Resource.State.SUCCESS) {
				val objMap = hashMapOf<String, Any>()
				objMap["wallet_id"] = 1
				requestTransactionsWithoutLogIn(fingerPrint, curBlockChainService, networkType)
				val res = curBlockChainService.getWalletBalance(objMap)
				if (res.isSuccessful) {
					val body = res.body()
					if (body != null) {
						if (body.success) {
							val walletBalanceJson = JSONObject(body.wallet_balance.toString())
							val balance = walletBalanceJson.getLong("confirmed_wallet_balance")
							val division =
								if (isThisNotChiaNetwork(networkType)) Math.pow(
									10.0,
									8.0
								) else Math.pow(
									10.0,
									12.0
								)
							val divideToTrillions = balance.toDouble() / division
							VLog.d("Got Balance from the server: $balance after division : $divideToTrillions for fingerPrint  -> $fingerPrint")
							val prevBalance =
								walletDao.getWalletByFingerPrint(fingerPrint)[0].balance
							if (prevBalance != divideToTrillions)
								walletDao.updateWalletBalance(divideToTrillions, fingerPrint)
						} else {
							VLog.d("Base Response is not success ")
						}
					} else {
						VLog.d("Body  is null in requesting wallet balance : $body")
					}
				} else {
					VLog.d("Response is not successful in requesting wallet balance  : ${res.message()}")
				}
			} else {
				VLog.e("LoginStatus is not success in requesting wallet balance: ${loginStatus.error}")
			}
		} catch (ex: java.lang.Exception) {
			VLog.d("Caught Exception gettingWalletBalance  : ${ex}")
		}
	}


	private suspend fun savingWalletToDB(
		privateKeyDto: PrivateKeyDto,
		address: String,
		mnemonics: List<String>,
		networkType: String
	) {
		var homeIsAdded = 0L
		val homeAddCounter = prefs.getHomeAddedCounter()
		if (homeAddCounter < 10) {
			prefs.increaseHomeAddedCounter()
			homeIsAdded = System.currentTimeMillis()
		}
		val encryptedMnemonics = getEncryptedMnemonics(mnemonics)
		val walletEntity =
			WalletEntity(
				privateKeyDto.fingerprint,
				privateKeyDto.pk,
				privateKeyDto.sk,
				address,
				encryptedMnemonics,
				networkType,
				homeIsAdded,
				0.0
			)
		val insertedRow = walletDao.insertWallet(walletEntity = walletEntity)
		VLog.d("InsertedRow Num : $insertedRow, WalletEntity : $walletEntity")
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

	suspend fun getPrivateKey(
		fingerPrint: Long,
		curBlockChainService: BlockChainService
	): PrivateKeyDto {
		val body = hashMapOf<String, Any>()
		body["fingerprint"] = fingerPrint
		val res = curBlockChainService.getPrivateKey(body = body)
		if (res.isSuccessful) {
			val baseRes = res.body()
			if (baseRes != null && baseRes.success) {
				val privateKeyDto = baseRes.private_key!!
				VLog.d("PrivateKeyDto : $privateKeyDto")
				return privateKeyDto
			}
		}
		throw  Exception(res.message())
	}

	override suspend fun logIn(
		fingerPrint: Long,
		curBlockChainService: BlockChainService
	): Resource<String> {
		try {
			val body = hashMapOf<String, Any>()
			body["fingerprint"] = fingerPrint
			val res = curBlockChainService.loginWithFingerPrint(body)
			if (res.isSuccessful) {
				val baseRes = res.body() ?: return Resource.error(Throwable(res.message()))
				if (baseRes.success) {
					return Resource.success("OK")
				} else {
					throw Exception("Exception login  ${res.message()}")
				}
			} else {
				VLog.d("Request is not success in login method  : ${res.body()}")

			}
		} catch (ex: Exception) {
			VLog.d("Exception in login caught : ${ex.message}")
		}
		return Resource.error(Throwable("Status is error in log in"))
	}


	override suspend fun updateTokensChiaNetwork() {
		try {
			val walletList = walletDao.getAllWalletList()

			for (wallet in walletList) {
				if (!isThisNotChiaNetwork(wallet.networkType)) {

					val networkItem = getNetworkItemFromPrefs(wallet.networkType)
						?: throw Exception("Exception in converting json str to networkItem")

					val curBlockChainService =
						retrofitBuilder.baseUrl(networkItem.wallet + "/").build()
							.create(BlockChainService::class.java)

					getWalletTokensForChiaNetwork(
						wallet.fingerPrint,
						curBlockChainService
					)
				}
			}

		} catch (ex: Exception) {
			VLog.d("Updating tokens for Chia Network Exception occurred : ${ex.message}")
		}
	}


	suspend fun generateNewAddress(curBlockChainService: BlockChainService): String {
		val body = hashMapOf<String, Any>()
		body["wallet_id"] = 1
		body["new_address"] = false
		val res = curBlockChainService.generateNewAddress(body)
		if (res.isSuccessful) {
			VLog.d("ResponseBody new Generating New Address : ${res.body()}")
			val jsonObject = JSONObject(res.body().toString())
			val success = jsonObject.getBoolean("success")
			if (success) {
				val newAddress = jsonObject.getString("address")
				VLog.d("NewAddress from generation : $newAddress")
				return newAddress
			}
		}
		throw  Exception(res.message())
	}


	override suspend fun getWalletTokensForChiaNetwork(
		fingerPrint: Long,
		curBlockChainService: BlockChainService
	) {
		try {

			val logInStatus = logIn(fingerPrint, curBlockChainService)
			if (logInStatus.state == Resource.State.SUCCESS) {
				VLog.d("Login status is success : for Chia Wallet  to get WalletTokens FingerPrint :  $fingerPrint")
				val hashMap = hashMapOf<String, Any>()
				hashMap["wallet_id"] = 1
				val res = curBlockChainService.getWallets(hashMap)
				if (res.isSuccessful) {

					val tokenList = res.body()!!.asJsonObject["wallets"].asJsonArray
					VLog.d("WalletList  for fingerPrint : $fingerPrint,  : $tokenList")
					val hashWithId = hashMapOf<String, Int>()
					for (i in 0 until tokenList.size()) {
						val data = tokenList[i].asJsonObject["data"].asString
						val id = tokenList[i].asJsonObject["id"].asInt
						if (data.isEmpty()) continue
						val hash = data.substring(0, data.length - 2)
						val tokenExist = tokenDao.getTokenByHash(hash)
						if (tokenExist.isPresent) {
							hashWithId[hash] = id
							VLog.d("Found token with hash from db for fingerPrint : $fingerPrint and hash : $hash")
						} else {
							VLog.d("Didn't find token with hash from db for fingerPrint : $fingerPrint and hash : $hash")
						}
					}
					val row = walletDao.updateChiaNetworkHashTokenIdWallet(fingerPrint, hashWithId)
					VLog.d("Updating wallet Tokens Hash and ID : $hashWithId for fingerPrint : $fingerPrint , row : $row")
				} else {
					VLog.d("Result is not success for updating tokens list for wallet $res")
				}
			}
		} catch (ex: java.lang.Exception) {
			VLog.d("Exception in getting walletTokens Chia Network : $ex")
		}
	}

	private suspend fun requestBalanceForChiaNetworkWallet(fingerPrint: Long, networkType: String) {
		try {

			val networkItem = getNetworkItemFromPrefs(networkType)
				?: throw Exception("Exception in converting json str to networkItem")

			val curBlockChainService =
				retrofitBuilder.baseUrl(networkItem.wallet + "/").build()
					.create(BlockChainService::class.java)

			val loginStatus = logIn(fingerPrint, curBlockChainService)
			if (loginStatus.state == Resource.State.SUCCESS) {
				val walletOpt = walletDao.getWalletByFingerPrint(fingerPrint)
				requestTransactionsWithoutLogIn(fingerPrint, curBlockChainService, networkType)
				if (walletOpt.isNotEmpty()) {
					val wallet = walletOpt[0]
					val hashWithAmount = wallet.hashWithAmount
					val chiaBalance =
						getConfirmedBalanceForChiaNetworkWallet(curBlockChainService, 1)
					hashWithAmount[networkType] =
						chiaBalance
					VLog.d("Token Balance for Chia Network fingerPrint : $fingerPrint Chia Network, balance : $chiaBalance")
					VLog.d("HashWithIdWallet for Chia Network : ${wallet.hashWithIdWallet}")
					for ((hash, walletId) in wallet.hashWithIdWallet) {
						val tokenBalance =
							getConfirmedBalanceForChiaNetworkWallet(curBlockChainService, walletId)
						VLog.d("Token Balance for Chia Network fingerPrint : $fingerPrint and hash : $hash, balance : $tokenBalance")
						hashWithAmount[hash] = tokenBalance
					}
					val row =
						walletDao.updateChiaNetworkHashTokenBalance(fingerPrint, hashWithAmount)
					VLog.d("HashWithIdWallet for Chia Network: ${wallet.hashWithAmount} and Row : $row , fingerPrint : $fingerPrint")
				} else {
					VLog.d("Did not find wallet by fingerPrint : $fingerPrint")
				}
			} else {
				VLog.d("Login status is not success for getting wallet balance for Chia Network")
			}

		} catch (ex: Exception) {
			VLog.d("Exception in requesting balance for chia network wallet : ${ex.message}")
		}
	}

	private suspend fun getConfirmedBalanceForChiaNetworkWallet(
		curBlockChainService: BlockChainService,
		walletId: Int
	): Double {
		try {
			val objMap = hashMapOf<String, Any>()
			objMap["wallet_id"] = walletId
			val res = curBlockChainService.getWalletBalance(objMap)
			if (res.isSuccessful) {
				val body = res.body()
				if (body != null) {
					if (body.success) {
						val walletBalanceJson = JSONObject(body.wallet_balance.toString())
						val balance = walletBalanceJson.getLong("confirmed_wallet_balance")
						return balance.toDouble() / 1000_000_000_000.0
					} else {
						VLog.d("Base Response is not success for getting confirmed balance : walletId : $walletId ")
					}
				} else {
					VLog.d("Body is null : $body")
				}
			} else {
				VLog.d("Response is not successful  : ${res.message()}")
			}

		} catch (ex: java.lang.Exception) {
			VLog.d("Exception in getting  confirmedBalance Chia Wallet WalletID : $walletId")
		}
		throw Exception("Throwing exception in Chia Network balance")
	}


	override suspend fun deleteAllKeys() {
		try {
			val distinctNetworkTypes = walletDao.getDistinctNetworkTypes()
			distinctNetworkTypes.forEach { type ->
				val networkItem = getNetworkItemFromPrefs(type)
					?: throw Exception("Exception in converting json str to networkItem")

				val curBlockChainService =
					retrofitBuilder.baseUrl(networkItem.wallet + "/").build()
						.create(BlockChainService::class.java)
				val res = curBlockChainService.delete_all_keys()
				if (res.isSuccessful) {

					val success = res.body()!!.success
					if (success) {
						VLog.d("Success is true in deleting all keys")
					} else {
						VLog.d("Success is false in deleting all keys")
					}

				} else {
					VLog.d("Response is not successful in deleting all keys : ${res.body()}")
				}
			}
		} catch (ex: Exception) {
			VLog.d("Exception in deleting all keys : $ex")
		}
	}

	override suspend fun updateBalanceAndTransactionsPeriodically() {
		val walletListDb = walletDao.getAllWalletList()
		for (wallet in walletListDb) {
			updateBalanceAndTransaction(wallet, false)
		}
	}

	override suspend fun push_tx(
		jsonSpendBundle: String,
		url: String
	): Resource<String> {

		try {
			VLog.d("Push method got called in data layer")
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
			val res = curBlockChainService.push_transaction(spenBundle)
			VLog.d("Result from push_transaction  ${res.body()} : ${res.body()!!.status}")
			if (res.isSuccessful) {
				val status = res.body()!!.status
				if (status == "SUCCESS") {
					return Resource.success("OK")
				}
			}
		} catch (ex: Exception) {
			VLog.d("Exception occured in puzsh_tx transaction : ${ex.message}")
		}
		return Resource.error(Throwable("ex"))
	}


	suspend fun updateBalanceAndTransaction(wallet: WalletEntity, isNewWallet: Boolean) {
		try {
			val networkItem = getNetworkItemFromPrefs(wallet.networkType)
				?: throw Exception("Exception in converting json str to networkItem")

			val curBlockChainService =
				retrofitBuilder.baseUrl(networkItem.full_node + "/").build()
					.create(BlockChainService::class.java)

			val body = hashMapOf<String, Any>()
			body["puzzle_hash"] =
				wallet.sk
			body["include_spent_coins"] = true
			val request = curBlockChainService.queryBalance(body)

			val division =
				if (isThisNotChiaNetwork(wallet.networkType)) Math.pow(
					10.0,
					8.0
				) else Math.pow(
					10.0,
					12.0
				)

			val allTrans = mutableListOf<TransactionEntity>()

			if (request.isSuccessful) {

				val coinRecordsJsonArray = request.body()!!["coin_records"].asJsonArray
				var sum = 0.0
				for (coin in coinRecordsJsonArray) {
					val jsCoin = coin.asJsonObject
					val amount =
						jsCoin.get("coin").asJsonObject.get("amount").asLong
					val blockHeight = jsCoin.get("confirmed_block_index").asLong
					val timeStamp = jsCoin.get("timestamp").asLong * 1000
					val trans_id = jsCoin.get("coin").asJsonObject.get("parent_coin_info").asString
					val spent = jsCoin.get("spent").asBoolean
					val afterDivision = amount / division
					if (!spent)
						sum += amount
					val status = if (spent) Status.Outgoing else Status.Incoming
					val trans = TransactionEntity(
						trans_id,
						afterDivision,
						timeStamp,
						blockHeight,
						status,
						wallet.networkType,
						"",
						wallet.fingerPrint,
						0.0
					)
					allTrans.add(trans)
				}

				val sumAfterDivision = sum / division
				val oldBalance = walletDao.getWalletByFingerPrint(wallet.fingerPrint).get(0).balance
				if (sumAfterDivision != oldBalance)
					walletDao.updateWalletBalance(sumAfterDivision, wallet.fingerPrint)
				VLog.d("Updating balance for : $sumAfterDivision and allTransList : $allTrans")
				for (tran in allTrans) {
					val tranExist =
						transactionDao.checkTransactionByIDExistInDB(tran.transaction_id)
					if (!tranExist.isPresent && !isNewWallet && tran.status == Status.Incoming) {
						notificationHelper.callGreenAppNotificationMessages(
							"You received new transaction",
							System.currentTimeMillis()
						)
					}
					transactionDao.insertTransaction(tran)
				}
			} else {
				VLog.d("Request is not success for updating balance and trans ")
			}
		} catch (ex: java.lang.Exception) {
			VLog.d("Exception in updating balance and trans : ${ex.message}")
		}
	}

}
