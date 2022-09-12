package com.android.greenapp.data.interact

import android.os.Build
import androidx.annotation.RequiresApi
import com.android.greenapp.data.local.TokenDao
import com.android.greenapp.data.local.TransactionDao
import com.android.greenapp.data.local.WalletDao
import com.android.greenapp.data.local.entity.TransactionEntity
import com.android.greenapp.data.local.entity.WalletEntity
import com.android.greenapp.data.network.BlockChainService
import com.android.greenapp.data.network.dto.greenapp.network.NetworkItem
import com.android.greenapp.data.network.dto.transaction.TransactionDto
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
import org.json.JSONObject
import retrofit2.Retrofit
import java.util.HashMap
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
        wallet: Wallet
    ) :Resource<String>{
        val key = encryptor.getAESKey(prefs.getSettingString(PrefsManager.PASSCODE, ""))
        val encMnemonics = encryptor.encrypt(wallet.mnemonics.toString(), key!!)
        val walletEntity = wallet.toWalletEntity(encMnemonics)
        walletDao.insertWallet(walletEntity = walletEntity)
//        updateTransactionsBalance(walletEntity, true, false)
        updateTransactionsBalance(walletEntity, true, true)
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
            updateTransactionsBalance(wallet, false, true)
//            updateTransactionsBalance(wallet, false, false)
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
            val res = curBlockChainService.pushTransaction(spenBundle)
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
                if (isThisNotChiaNetwork(wallet.networkType)) Math.pow(
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
                    val parentAmountHash =
                        getParentCoinsSpentAmountAndHashValue(
                            parent_coin_info,
                            curBlockChainService
                        )
                    var status = Status.Outgoing
                    if (parentAmountHash != null) {
                        val parentAmount = parentAmountHash["amount"] as Long
                        val parentHash = parentAmountHash["puzzle_hash"] as String
                        var amount: Long
                        if (parentHash.substring(2) == wallet.sk) {
                            amount = parentAmount - curAmount
                            if (amount < 0) {
                                VLog.d("Amount is minus on transaction : $timeStamp")
                            }
                        } else {
                            amount = curAmount
                            status = Status.Incoming
                        }
                        val transactionDto = TransactionDto(
                            parent_coin_info+wallet.fingerPrint,
                            timeStamp,
                            amount / division,
                            true,
                            confirmed_block_index,
                            0,
                            "",
                            0.0
                        )
                        val tranEntity = transactionDto.toTransactionEntity(
                            wallet.fingerPrint,
                            status,
                            wallet.networkType,
                            amount / division,
                            timeStamp
                        )
                        allTrans.add(tranEntity)
                    } else {
                        VLog.d("Retrieve ParentAmountHash is null on transaction")
                    }
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

    suspend fun getParentCoinsSpentAmountAndHashValue(
        parent_info: String,
        service: BlockChainService
    ): HashMap<Any, Any>? {
        val res = hashMapOf<Any, Any>()
        val body = hashMapOf<String, Any>()
        body["name"] = parent_info
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

    suspend fun updateBalanceAndTransactionDeprecated(wallet: WalletEntity, isNewWallet: Boolean) {
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
                    val trans_id =
                        jsCoin.get("coin").asJsonObject.get("parent_coin_info").asString
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
                val oldBalance =
                    walletDao.getWalletByFingerPrint(wallet.fingerPrint).get(0).balance
                if (sumAfterDivision != oldBalance)
                    walletDao.updateWalletBalance(sumAfterDivision, wallet.fingerPrint)
                VLog.d("Updating balance for : $sumAfterDivision and allTransList : $allTrans")
                for (tran in allTrans) {
                    val tranExist =
                        transactionDao.checkTransactionByIDExistInDB(tran.transaction_id)
                    if (!tranExist.isPresent && isNewWallet && tran.status == Status.Incoming) {
                        notificationHelper.callGreenAppNotificationMessages(
                            "You received new transaction",
                            System.currentTimeMillis()
                        )
                    }
//                    transactionDao.insertTransaction(tran)
                }
            } else {
                VLog.d("Request is not success for updating balance and trans ")
            }
        } catch (ex: java.lang.Exception) {
            VLog.d("Exception in updating balance and trans : ${ex.message}")
        }
    }


}
