package com.green.wallet.data.interact

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.common.tools.getTokenPrecisionByCode
import com.google.gson.Gson
import com.green.wallet.data.local.*
import com.green.wallet.data.local.entity.NFTCoinEntity
import com.green.wallet.data.local.entity.NFTInfoEntity
import com.green.wallet.data.local.entity.OfferTransactionEntity
import com.green.wallet.data.local.entity.SpentCoinsEntity
import com.green.wallet.data.local.entity.TransactionEntity
import com.green.wallet.data.local.entity.WalletEntity
import com.green.wallet.data.network.BlockChainService
import com.green.wallet.data.network.dto.coinSolution.ParentCoinRecordResponse
import com.green.wallet.data.network.dto.greenapp.network.NetworkItem
import com.green.wallet.data.network.dto.spendbundle.CoinDto
import com.green.wallet.data.network.dto.spendbundle.CoinSpend
import com.green.wallet.data.network.dto.spendbundle.SpenBunde
import com.green.wallet.data.network.dto.spendbundle.SpendBundle
import com.green.wallet.data.preference.PrefsManager
import com.green.wallet.domain.domainmodel.NFTInfo
import com.green.wallet.domain.domainmodel.NftOfferCoin
import com.green.wallet.domain.domainmodel.PushResult
import com.green.wallet.domain.domainmodel.Wallet
import com.green.wallet.domain.interact.*
import com.green.wallet.presentation.App
import com.green.wallet.presentation.custom.*
import com.green.wallet.presentation.custom.encryptor.EncryptorProvider
import com.green.wallet.presentation.tools.METHOD_CHANNEL_GENERATE_HASH
import com.green.wallet.presentation.tools.PRECISION_XCH
import com.green.wallet.presentation.tools.Resource
import com.green.wallet.presentation.tools.Status
import com.green.wallet.presentation.tools.VLog
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import retrofit2.Retrofit
import java.util.*
import javax.inject.Inject
import kotlin.system.measureTimeMillis


class BlockChainInteractImpl @Inject constructor(
    private val walletDao: WalletDao,
    private val prefs: PrefsInteract,
    private val transactionDao: TransactionDao,
    private val retrofitBuilder: Retrofit.Builder,
    private val gson: Gson,
    private val tokenDao: TokenDao,
    private val encryptor: EncryptorProvider,
    private val notificationHelper: NotificationHelper,
    private val spentCoinsInteract: SpentCoinsInteract,
    private val spentCoinsDao: SpentCoinsDao,
    private val greenAppInteract: GreenAppInteract,
    private val context: Context,
    private val nftCoinsDao: NftCoinsDao,
    private val nftInfoDao: NftInfoDao,
    private val offerTransactionDao: OfferTransactionDao,
    private val cancelTransactionDao: CancelTransactionDao
) : BlockChainInteract {

    private val mutexUpdateBalance = Mutex()

    private val handler = CoroutineExceptionHandler { _, th ->
        VLog.d("Exception caught in blockchain : ${th.message}")
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override suspend fun saveNewWallet(
        wallet: Wallet, imported: Boolean
    ): Resource<String> {
        val key = prefs.getSettingString(PrefsManager.PASSCODE, "")
        val encMnemonics = encryptor.encrypt(wallet.mnemonics.toString(), key)
        var savedTime = greenAppInteract.getServerTime()
        if (savedTime == -1L) savedTime = System.currentTimeMillis()
        val walletEntity = wallet.toWalletEntity(encMnemonics, savedTime)
        VLog.d("Inserting wallet Entity mnemonics : ${walletEntity.encMnemonics}")
        walletDao.insertWallet(walletEntity = walletEntity)
        if (imported) {
            CoroutineScope(Dispatchers.IO + handler).launch {
                updateWalletNFTBalance(walletEntity)
            }
            val timeTaken = measureTimeMillis {
                val job = CoroutineScope(Dispatchers.IO + handler).launch {
                    launch {
                        updateWalletBalance(walletEntity)
                    }
                    launch {
                        updateTokenBalanceSpeedily(walletEntity)
                    }
                }
                job.join()
            }
            VLog.d("Time taken: ${timeTaken / 1000}s  to get balance")
        }
        //generating hashes for tokens later
        CoroutineScope(Dispatchers.IO + handler).launch {
            val hashListImported = hashMapOf<String, List<String>>()
            val methodChannel = MethodChannel(
                (context.applicationContext as App).flutterEngine.dartExecutor.binaryMessenger,
                METHOD_CHANNEL_GENERATE_HASH
            )
            val defaultTokenOnMainScreen = tokenDao.getTokensDefaultOnScreen().map { it.hash }
            var counterTokenHash = 0
            methodChannel.setMethodCallHandler { method, calLBack ->
                VLog.d("Got back method from hash : ${method.method}")
                val hashTokenMethod = method.method
                if (defaultTokenOnMainScreen.contains(hashTokenMethod)) {
                    val args = method.arguments as HashMap<*, *>
                    val outer_hashes = args[hashTokenMethod]!! as List<String>
                    hashListImported[hashTokenMethod] = outer_hashes
                    counterTokenHash++
                    VLog.d("Got back counter token hash : $counterTokenHash")
                    if (counterTokenHash == defaultTokenOnMainScreen.size) {
//						VLog.d("HashList Imported on creating new wallet : $hashListImported")
                        CoroutineScope(Dispatchers.IO).launch {
                            walletDao.updateChiaNetworkHashListImportedByAddress(
                                wallet.address, hashListImported
                            )
                        }
                    }
                }
            }
            for (token in defaultTokenOnMainScreen) {
                val map = hashMapOf<String, String>()
                map["puzzle_hashes"] = convertListToStringWithSpace(wallet.puzzle_hashes)
                map["asset_id"] = token
                withContext(Dispatchers.Main) {
                    methodChannel.invokeMethod("asyncCatPuzzle", map)
                }
            }
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
                updateWalletBalanceWithTransactions(wallet)
                updateTokenBalanceWithFullNode(wallet)
//                updateWalletNFTBalance(wallet)
                updateOfferTransactions(wallet)
                updateCancelTransaction(wallet)
            }
        }
    }

    private suspend fun updateCancelTransaction(wallet: WalletEntity) {
        try {
            if (isThisChivesNetwork(wallet.networkType)) return
            val networkItem = getNetworkItemFromPrefs(wallet.networkType)
                ?: throw Exception("Exception in converting json str to networkItem")
            val service = retrofitBuilder.baseUrl(networkItem.full_node + "/").build()
                .create(BlockChainService::class.java)
            val cancelTransList =
                cancelTransactionDao.getCancelTransactionListByWalletAddress(wallet.address)
            VLog.d("Cancel Trans List  : $cancelTransList")
            for (cancel in cancelTransList) {
                val xchCoin =
                    spentCoinsDao.getSpentCoinsByTranTimeCreatedCode(cancel.createAtTime, "XCH")[0]
                val spentHeight = spentHeightForXCHCoin(service, xchCoin)
                if (spentHeight != -1L) {
                    cancelTransactionDao.deleteCancelTransaction(cancel)

                    notificationHelper.callGreenAppNotificationMessages(
                        "Canceling offer is completed", System.currentTimeMillis()
                    )

                    spentCoinsDao.deleteSpentConsByTimeCreated(cancel.createAtTime)

                    val offerTransaction =
                        offerTransactionDao.getOfferTransactionByTranID(cancel.offerTranID).get()
                    spentCoinsDao.deleteSpentConsByTimeCreated(offerTransaction.createdTime)
                    offerTransactionDao.updateOfferTransactionStatusHeight(
                        status = Status.CANCELLED,
                        height = spentHeight.toInt(),
                        tranId = cancel.offerTranID
                    )
                }
            }
        } catch (ex: Exception) {
            VLog.d("Exception occurred when updateCancelTransaction : ${ex.message}")
        }
    }

    private suspend fun updateOfferTransactions(wallet: WalletEntity) {
        try {
            if (isThisChivesNetwork(wallet.networkType)) return
            val networkItem = getNetworkItemFromPrefs(wallet.networkType)
                ?: throw Exception("Exception in converting json str to networkItem")
            val service = retrofitBuilder.baseUrl(networkItem.full_node + "/").build()
                .create(BlockChainService::class.java)
            val offersTrans = offerTransactionDao.getAllOfferTransactionsByAddressFk(wallet.address)
            for (offer in offersTrans) {
                if (offer.acceptOffer) {
                    VLog.d("OfferTrans AcceptOffer true : $offersTrans")
                    updateTakingOfferTransactionStatus(offer, service)
                } else if (offer.status != Status.CANCELLING) {

                }
            }
        } catch (ex: Exception) {
            VLog.d("Exception occurred when updateOfferTransactions : ${ex.message}")
        }
    }

    private suspend fun updateTakingOfferTransactionStatus(
        offer: OfferTransactionEntity,
        service: BlockChainService
    ) {
        val coin = spentCoinsDao.getSpentCoinsByTranTimeCreatedCode(offer.createdTime, "XCH")[0]
        val spentHeight = spentHeightForXCHCoin(service, coin)
        if (spentHeight != -1L) {
            offerTransactionDao.updateOfferTransaction(
                height = spentHeight.toInt(),
                tranId = offer.tranId
            )
            notificationHelper.callGreenAppNotificationMessages(
                "Take offer is completed", System.currentTimeMillis()
            )
            spentCoinsDao.deleteSpentConsByTimeCreated(offer.createdTime)
        }
    }

    private suspend fun spentHeightForXCHCoin(
        service: BlockChainService,
        coin: SpentCoinsEntity
    ): Long {
        val body = hashMapOf<String, Any>()
        body["parent_ids"] =
            listOf(coin.parent_coin_info)
        body["include_spent_coins"] = true
        val res =
            service.getCoinRecordsByParentIds(body).body()?.coin_records
        for (curCoin in res ?: return -1L) {
            val multi = (coin.amount * PRECISION_XCH).toLong()
            if (curCoin.coin.amount == multi && curCoin.spent_block_index != 0L) {
                return curCoin.spent_block_index
            }
        }
        return -1L
    }

    private suspend fun updateWalletNFTBalance(wallet: WalletEntity) {
        try {
            if (isThisChivesNetwork(wallet.networkType)) return
            val networkItem = getNetworkItemFromPrefs(wallet.networkType)
                ?: throw Exception("Exception in converting json str to networkItem")
            val curBlockChainService = retrofitBuilder.baseUrl(networkItem.full_node + "/").build()
                .create(BlockChainService::class.java)
            val methodChannel = MethodChannel(
                (context.applicationContext as App).flutterEngine.dartExecutor.binaryMessenger,
                METHOD_CHANNEL_GENERATE_HASH
            )
            for (hash in wallet.puzzle_hashes) {
                nftBalanceByHash(
                    wallet,
                    hash,
                    curBlockChainService,
                    networkItem,
                    methodChannel
                )
            }
        } catch (ex: Exception) {
            VLog.d("Exception occurred with updating wallet nft balance by individ hash : ${ex.message}")
        }
    }

    private suspend fun nftBalanceByHash(
        wallet: WalletEntity,
        hash: String,
        service: BlockChainService,
        networkItem: NetworkItem,
        methodChannel: MethodChannel
    ) {
        try {
            val body = hashMapOf<String, Any>()
            body["hint"] = hash
            body["include_spent_coins"] = false
            val res = service.getCoinRecordsByHint(body)
            if (res.isSuccessful) {
                val coinRecords = res.body()!!.coin_records.filter { it.coin.amount == 1L }
                for (coin in coinRecords) {
                    val nftCoin = nftCoinsDao.getNFTCoinByParentCoinInfo(coin.coin.parent_coin_info)
                    if (nftCoin.isPresent) continue

                    val nftCoinEntity = NFTCoinEntity(
                        coin.coin.parent_coin_info,
                        address_fk = wallet.address,
                        coin.coin.puzzle_hash,
                        1,
                        coin.confirmed_block_index,
                        coin.spent_block_index,
                        coin.timestamp,
                        hash
                    )
//                    VLog.d("NFTCoinEntity Hash : $hash Saving NFTCoinEntity : $nftCoinEntity")
//                    VLog.d("NFTCoinEntity CoinName : ${coin.coin.parent_coin_info} Height : ${coin.confirmed_block_index}")

                    //4942127
                    //nftCoinName : 4a4bfcc0dcd992564fa5615f0cb4d46189c0afab352ebca463820f98f8c22fef
                    //nftHash: 974b8fd6fcd9d87a6cee9bc8ba403749e469ea9931d3ef2476d386032baab897

                    val nftInfoEntity = getNFTINfoFromWalletApi(
                        networkItem,
                        coin.coin.parent_coin_info,
                        address_fk = wallet.address,
                        methodChannel
                    )
                    if (nftInfoEntity == null) {
                        VLog.d("NFTInfo Entity is null ")
                        return
                    }
                    VLog.d("Inserting nftInfo to db : $nftInfoEntity")
                    nftInfoDao.insertNftInfoEntity(nftInfoEntity)
                    nftCoinsDao.insertNftCoinsEntity(nftCoinEntity)
                    if (nftCoinEntity.time_stamp * 1000L >= wallet.savedTime) {
                        val resLanguageResource =
                            prefs.getSettingString(PrefsManager.LANGUAGE_RESOURCE, "")
                        val resMap = Converters.stringToHashMap(resLanguageResource)
                        val incomingTransaction =
                            resMap["push_notifications_incoming"] ?: "Incoming transaction"
                        val tran = TransactionEntity(
                            transaction_id = UUID.randomUUID().toString(),
                            amount = 1.0,
                            created_at_time = nftCoinEntity.time_stamp * 1000L,
                            height = nftCoinEntity.confirmed_block_index,
                            status = Status.Incoming,
                            networkType = wallet.networkType,
                            to_dest_hash = wallet.puzzle_hashes[0],
                            fkAddress = wallet.address,
                            fee_amount = 0.0,
                            code = "NFT",
                            confirm_height = 0,
                            nft_coin_hash = nftInfoEntity.nft_coin_hash
                        )
                        transactionDao.insertTransaction(tran)
                        notificationHelper.callGreenAppNotificationMessages(
                            "$incomingTransaction : +1 NFT",
                            System.currentTimeMillis()
                        )
                    }
                }
            } else {
                VLog.d("Request nft coin records is no success : ${res.code()}")
            }
        } catch (ex: Exception) {
            VLog.d("Exception occurred with nft balance by hash : ${ex.message}")
        }
    }

    private suspend fun getNFTINfoFromWalletApi(
        networkItem: NetworkItem,
        parentCoinInfo: String,
        address_fk: String,
        methodChannel: MethodChannel
    ): NFTInfoEntity? {
        try {

            val walletService = retrofitBuilder.baseUrl(networkItem.wallet + '/').build()
                .create(BlockChainService::class.java)
            val body = hashMapOf<String, Any>()
            body["coin_id"] = parentCoinInfo
            body["latest"] = true
            body["ignore_size_limit"] = false
            body["reuse_puzhash"] = true
            val reqNFTInfo = walletService.getNFTInfoByCoinId(body)
            if (reqNFTInfo.isSuccessful) {
                val nftInfo = reqNFTInfo.body()!!.nft_info
                VLog.d("Retrieved NFT Info from wallet : $nftInfo and with Coin_Id : $parentCoinInfo")
                var minterDid = ""
                var waitFlutter = true
                methodChannel.setMethodCallHandler { call, method ->
                    if (call.method == "puzzle_hash_to_address") {
                        minterDid = call.arguments.toString()
                        waitFlutter = false
                    } else if (call.method == "exception") {
                        waitFlutter = false
                    }
                }
                withContext(Dispatchers.Main) {
                    methodChannel.invokeMethod(
                        "puzzle_hash_to_address",
                        nftInfo.minter_did ?: ""
                    )
                }
                val metaData = getMetaDataNFT(nftInfo.metadata_uris?.get(0) ?: "")
                if (metaData == null) {
                    VLog.d("Meta Data of nft is null")
                    return null
                }
                var c = 0
                while (waitFlutter) {
                    c++
                    delay(1000)
                    VLog.d("Waiting to minter did : $c")
                    if (c >= 5)
                        return null
                }
                val description = metaData["description"].toString()
                val collection = metaData["collection"].toString()
                val name = metaData["name"].toString()
                val properties = metaData["attributes"] as HashMap<String, String>
                return NFTInfoEntity(
                    nft_coin_hash = parentCoinInfo,
                    nft_id = nftInfo.nft_id ?: "",
                    launcher_id = nftInfo.launcher_id ?: "",
                    owner_did = nftInfo.owner_did ?: "",
                    minter_did = minterDid,
                    royalty_percentage = nftInfo.royalty_percentage / 100,
                    mint_height = nftInfo.mint_height,
                    data_url = nftInfo.data_uris?.get(0) ?: "",
                    data_hash = nftInfo.data_hash ?: "",
                    meta_hash = nftInfo.metadata_hash ?: "",
                    meta_url = nftInfo.metadata_uris?.get(0) ?: "",
                    description = description,
                    collection = collection,
                    properties = properties,
                    name = name,
                    address_fk = address_fk,
                    spent = false,
                    isPending = false,
                    timePending = 0L
                )
            } else {
                VLog.d("Request is no success for nftInfo : ${reqNFTInfo.raw()}")
            }

        } catch (ex: Exception) {
            VLog.d("Exception occurred in getting nft info from wallet : ${ex.message}")
        }
        return null
    }

    private suspend fun getMetaDataNFT(metaDataUrlJson: String): HashMap<String, Any>? {
        try {
            val res = retrofitBuilder.build().create(BlockChainService::class.java)
                .getMetaDataNFTJson(metaDataUrlJson)
            VLog.d("MetaDataJson NFT : ${res.body()}")
            val resJson = res.body()!!.asJsonObject
            val resMap = hashMapOf<String, Any>()
            val description = resJson["description"].toString()
            val collection = resJson["collection"].asJsonObject["name"].toString()
            val name = resJson["name"].toString()
            resMap["description"] = description.substring(1, description.length - 1)
            resMap["collection"] = collection.substring(1, collection.length - 1)
            resMap["name"] = name.substring(1, name.length - 1)
            val attributeMap = hashMapOf<String, String>()
            if (resJson["attributes"] != null) {
                val attJsonArray = resJson["attributes"].asJsonArray
                for (attr in attJsonArray) {
                    attr.asJsonObject["trait_type"] ?: continue
                    attr.asJsonObject["value"] ?: continue
                    val trait = attr.asJsonObject["trait_type"].asString
                    val value = attr.asJsonObject["value"].asString
                    VLog.d("TraitType : $trait and Value : $value of nft attributes")
                    attributeMap[trait] = value
                }
            }
            resMap["attributes"] = attributeMap
            return resMap
        } catch (ex: Exception) {
            VLog.d("Exception in getting meta data json with url $metaDataUrlJson Exception : $ex")
            return null
        }
    }

    private suspend fun getNftParentCoin(
        coin_hash: String, height: Long, service: BlockChainService
    ): ParentCoinRecordResponse? {
        try {
            val body = hashMapOf<String, Any>()
            body["coin_id"] = coin_hash
            body["height"] = height
            val res = service.getPuzzleAndSolution(body)
            if (res.isSuccessful) {
                return res.body()!!
            } else {
                VLog.d("Request is no success : ${res.errorBody()}")
            }
        } catch (ex: Exception) {
            VLog.d("Exception in getting parent coin of nft : ${ex.message}")
        }
        return null
    }

    private suspend fun updateInProgressTransactions() {
        try {
            val inProgressTrans = transactionDao.getTransactionsByStatus(Status.InProgress)
            for (tran in inProgressTrans) {
                if (tran.code == "NFT") {
                    //in case of nft
                    val height = searchForSpentNFTByPuzzleHashAndCoin(tran)
                    VLog.d("Height of unspent tran $tran : $height")
                    if (height != 0) {
                        transactionDao.updateTransactionStatusHeightNFTByTimeCreated(
                            Status.Outgoing, height.toLong(), tran.created_at_time
                        )
                        var c = nftInfoDao.updateSpentNFTInfoByNFTCoinHash(true, tran.nft_coin_hash)
                        c += nftCoinsDao.deleteNFTCoinEntityByCoinInfo(tran.nft_coin_hash)
                        VLog.d("Updating nft transaction height : ${tran.transaction_id} and deleting nftcoininfo : $c")
                        val deleteSpentCoinsRow =
                            spentCoinsDao.deleteSpentConsByTimeCreated(tran.created_at_time)
                        VLog.d("Affected rows when deleting spentCoins for nft : $deleteSpentCoinsRow")
                        val formatted = formattedDoubleAmountWithPrecision(tran.amount)
                        val resLanguageResource =
                            prefs.getSettingString(PrefsManager.LANGUAGE_RESOURCE, "")
                        val resMap = Converters.stringToHashMap(resLanguageResource)
                        val outgoing_transaction =
                            resMap["push_notifications_outgoing"] ?: "Outgoing transaction"
                        notificationHelper.callGreenAppNotificationMessages(
                            "$outgoing_transaction : $formatted ${tran.code}",
                            System.currentTimeMillis()
                        )
                    }

                } else {
                    val unSpentCoinRecordHeight = checkingUnSpentCoinHeightByTran(tran)
                    VLog.d("Trying to update coin Record Height for inProgress transaction : $tran : $unSpentCoinRecordHeight")
                    if (unSpentCoinRecordHeight != -1L) {
                        transactionDao.updateTransactionStatusHeight(
                            Status.Outgoing, unSpentCoinRecordHeight, tran.transaction_id
                        )
                        val deleteSpentCoinsRow =
                            spentCoinsDao.deleteSpentConsByTimeCreated(tran.created_at_time)
                        VLog.d("Affected rows when deleting spentCoins : $deleteSpentCoinsRow")
                        val formatted = formattedDoubleAmountWithPrecision(tran.amount)
                        val resLanguageResource =
                            prefs.getSettingString(PrefsManager.LANGUAGE_RESOURCE, "")
                        val resMap = Converters.stringToHashMap(resLanguageResource)
                        val outgoingTransaction =
                            resMap["push_notifications_outgoing"] ?: "Outgoing transaction"
                        notificationHelper.callGreenAppNotificationMessages(
                            "$outgoingTransaction : $formatted ${tran.code}",
                            System.currentTimeMillis()
                        )
                    }
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
            val curBlockChainService = retrofitBuilder.baseUrl(networkItem.full_node + "/").build()
                .create(BlockChainService::class.java)
            val body = hashMapOf<String, Any>()
            body["puzzle_hash"] = tran.to_dest_hash
            body["include_spent_coins"] = false
            val division = getTokenPrecisionByCode(tran.code)
            val request = curBlockChainService.queryBalanceWithSorting(body)
            if (request.isSuccessful) {
                val coinRecordsIncreasing =
                    request.body()!!.coin_records.sortedWith { p0, p1 -> if (p0.timestamp >= p1.timestamp) 1 else -1 }
//				VLog.d("CoinRecordsIncreasing to confirm incoming transaction : $coinRecordsIncreasing")
                for (record in coinRecordsIncreasing) {
                    val coinAmount = record.coin.amount / division
                    val timeStamp = record.timestamp
//					VLog.d("TimeStamp of updating trans : timeStamp : ${timeStamp} trantime : ${tran.created_at_time}, CoinAmount : $coinAmount , TranCoinAmount : ${tran.amount}")
                    if (coinAmount == tran.amount && timeStamp >= tran.created_at_time / 1000 - 120) {
                        return record.confirmed_block_index
                    }
                }
            }
        } catch (ex: java.lang.Exception) {
            VLog.d("Exception in getting unspent transactions coin records :  $ex")
        }
        return -1
    }

    private suspend fun checkingUnSpentCoinHeightByTran(tran: TransactionEntity): Long {
        try {
            val networkItem = getNetworkItemFromPrefs(tran.networkType)
                ?: throw Exception("Exception in converting json str to networkItem")
            val curBlockChainService = retrofitBuilder.baseUrl(networkItem.full_node + "/").build()
                .create(BlockChainService::class.java)
            val coinsByTranTime = spentCoinsDao.getSpentCoinsByTranTimeCreated(tran.created_at_time)
            if (coinsByTranTime.isNotEmpty()) {
                val coin = coinsByTranTime[0]
                return checkingUnSpentCoin(coin, curBlockChainService)
            }
        } catch (ex: Exception) {
            VLog.d("Exception in checking unSpent height by tran :$ex")
        }
        return -1
    }

    private suspend fun checkingUnSpentCoin(
        coin: SpentCoinsEntity,
        service: BlockChainService
    ): Long {
        val targetAmount = (coin.amount * getTokenPrecisionByCode(coin.code)).toLong()
        val body = hashMapOf<String, Any>()
        body["parent_ids"] =
            listOf(coin.parent_coin_info)
        body["include_spent_coins"] = true
        val coinsByParentIdRes =
            service.getCoinRecordsByParentIds(body)
        VLog.d("Coins by parents ids for transaction : ${coinsByParentIdRes.body()} : TargetAmount : $targetAmount")
        if (coinsByParentIdRes.isSuccessful) {
            for (c in coinsByParentIdRes.body()?.coin_records ?: return -1) {
                if (c.coin.amount >= targetAmount && c.coin.parent_coin_info == coin.parent_coin_info && c.spent)
                    return c.spent_block_index
            }
        }
        return -1
    }

    private suspend fun searchForSpentNFTByPuzzleHashAndCoin(tran: TransactionEntity): Int {
        try {
            val networkItem = getNetworkItemFromPrefs(tran.networkType)
                ?: throw Exception("Exception in converting json str to networkItem")
            val service = retrofitBuilder.baseUrl(networkItem.full_node + "/").build()
                .create(BlockChainService::class.java)
            val body = hashMapOf<String, Any>()
            body["hint"] = tran.to_dest_hash
            body["include_spent_coins"] = true
            body["start_height"] = tran.confirm_height
            body["end_height"] = tran.confirm_height + 1

            val res = service.getCoinRecordsByHint(body)
            if (res.isSuccessful) {
                val coinRecords = res.body()!!.coin_records
                for (coinNFT in coinRecords) {
                    if (coinNFT.coin.parent_coin_info == tran.nft_coin_hash && coinNFT.spent_block_index != 0L) {
                        return coinNFT.spent_block_index.toInt()
                    }
                }
            }
        } catch (ex: Exception) {
            VLog.d("Exception in getting unspent transactions ntf coin records :  $ex")
        }
        return 0
    }

    override suspend fun push_tx(
        jsonSpendBundle: String,
        url: String,
        sendAmount: Double,
        networkType: String,
        fingerPrint: Long,
        code: String,
        destPuzzleHash: String,
        address: String,
        fee: Double,
        spentCoinsJson: String,
        spentCoinsToken: String
    ): Resource<PushResult> {
        try {
            val serverTime = greenAppInteract.getServerTime() - 1000 * 60 * 2
            if (serverTime == -1L)
                throw ServerMaintenanceExceptions()
            VLog.d("Push method got called in data layer code : $networkType")
            val curBlockChainService =
                retrofitBuilder.baseUrl("$url/").build().create(BlockChainService::class.java)

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
                        serverTime,
                        0,
                        Status.InProgress,
                        networkType,
                        destPuzzleHash,
                        address,
                        fee,
                        code,
                        0
                    )
                    VLog.d("Inserting transaction entity : $trans and coinJson $spentCoinsJson and coinToken : $spentCoinsToken")
                    if (sendAmount != -1.0)
                        transactionDao.insertTransaction(trans)
                    if (spentCoinsJson.isNotEmpty())
                        spentCoinsInteract.insertSpentCoinsJson(
                            spentCoinsJson,
                            serverTime,
                            getShortNetworkType(networkType),
                            address
                        )
                    if (spentCoinsToken.isNotEmpty())
                        spentCoinsInteract.insertSpentCoinsJson(
                            spentCoinsToken, serverTime, code, address
                        )
                    return Resource.success(PushResult("OK", serverTime))
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

    private suspend fun updateTokenBalanceSpeedily(wallet: WalletEntity) {
        try {
            if (isThisChivesNetwork(wallet.networkType)) return
            val networkItem = getNetworkItemFromPrefs(wallet.networkType)
                ?: throw Exception("Exception in converting json str to networkItem")
            val curBlockChainService = retrofitBuilder.baseUrl(networkItem.full_node + "/").build()
                .create(BlockChainService::class.java)
            val hashWithAmount = hashMapOf<String, Double>()
            val job = CoroutineScope(Dispatchers.IO + handler).launch {
                for ((asset_id, puzzle_hashes) in wallet.hashListImported) {
                    launch {
                        VLog.d("Launched job to get balance for $asset_id and Puzzle Hash : $puzzle_hashes")
                        updateTokenBalanceIndividually(
                            curBlockChainService, asset_id, puzzle_hashes, hashWithAmount
                        )
                    }
                }
            }
            job.join()
            walletDao.updateChiaNetworkHashTokenBalanceByAddress(wallet.address, hashWithAmount)
        } catch (ex: java.lang.Exception) {
            VLog.d("Update token balance speedily exception : ${ex.message}")
        }
    }

    private suspend fun updateTokenBalanceIndividually(
        blockChainService: BlockChainService,
        assetId: String,
        puzzle_hashes: List<String>,
        hashWithAmount: HashMap<String, Double>
    ) {
        try {
            val body = hashMapOf<String, Any>()
            body["puzzle_hashes"] = puzzle_hashes
            body["include_spent_coins"] = false
            val division = 1000.0
            var balance = 0L
            val request = blockChainService.queryBalanceByPuzzleHashes(body)
            if (request.isSuccessful) {
                val coinRecordsJsonArray = request.body()!!.coin_records
                for (coin in coinRecordsJsonArray) {
                    val curAmount = coin.coin.amount
                    val spent = coin.spent
                    if (!spent) balance += curAmount
                }
            }
            val newAmount = balance / division
            hashWithAmount[assetId] = newAmount
        } catch (ex: Exception) {
            VLog.d("Exception caught in update token balance individually : ${ex.message}")
        }
    }

    override suspend fun updateTokenBalanceWithFullNode(wallet: WalletEntity) {
        try {
            if (isThisChivesNetwork(wallet.networkType)) return
            val networkItem = getNetworkItemFromPrefs(wallet.networkType)
                ?: throw Exception("Exception in converting json str to networkItem")

            val curBlockChainService = retrofitBuilder.baseUrl(networkItem.full_node + "/").build()
                .create(BlockChainService::class.java)
            val hashWithAmount = hashMapOf<String, Double>()
            for ((asset_id, puzzle_hashes) in wallet.hashListImported) {
                val body = hashMapOf<String, Any>()
                body["puzzle_hashes"] = puzzle_hashes
                body["include_spent_coins"] = false
                val division = 1000.0
                var balance = 0L
                val prevAmount = wallet.hashWithAmount[asset_id] ?: 0.0
                val request = curBlockChainService.queryBalanceByPuzzleHashes(body)
                if (request.isSuccessful) {
                    val coinRecordsJsonArray = request.body()!!.coin_records
                    for (coin in coinRecordsJsonArray) {
                        val curAmount = coin.coin.amount
                        val spent = coin.spent
                        if (!spent) balance += curAmount
                    }
                }
                val newAmount = balance / division
                hashWithAmount[asset_id] = newAmount
                if (newAmount != prevAmount) {
                    getIncomingTransactionNotifForToken(
                        puzzle_hashes, wallet, curBlockChainService, asset_id
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
            body["puzzle_hashes"] = puzzleHashes
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
                        parent_coin_info, curBlockChainService
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
                            code,
                            0
                        )
                        val formatted = formattedDoubleAmountWithPrecision(coinAmount / division)
                        val tokenCode = tokenDao.getTokenByHash(assetId).get().code
                        val resLanguageResource =
                            prefs.getSettingString(PrefsManager.LANGUAGE_RESOURCE, "")
                        val resMap = Converters.stringToHashMap(resLanguageResource)
                        val incoming_transaction =
                            resMap["push_notifications_incoming"] ?: "Incoming transaction"
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

    suspend fun updateWalletBalance(wallet: WalletEntity) {
        try {
            val networkItem = getNetworkItemFromPrefs(wallet.networkType)
                ?: throw Exception("Exception in converting json str to networkItem")

            val curBlockChainService = retrofitBuilder.baseUrl(networkItem.full_node + "/").build()
                .create(BlockChainService::class.java)
            val body = hashMapOf<String, Any>()
            body["puzzle_hashes"] = wallet.puzzle_hashes
            body["include_spent_coins"] = false
            val division = if (isThisChivesNetwork(wallet.networkType)) Math.pow(
                10.0, 8.0
            ) else Math.pow(
                10.0, 12.0
            )
            var balance = 0L
            val request = curBlockChainService.queryBalanceByPuzzleHashes(body)
            var newStartHeight = Long.MAX_VALUE
            if (request.isSuccessful) {
//				VLog.d("Got coin records : ${request.body()!!.coin_records}")
                for (coin in request.body()!!.coin_records) {
                    val curAmount = coin.coin.amount
                    val spent = coin.spent
                    newStartHeight = Math.min(newStartHeight, coin.confirmed_block_index)
                    if (!spent) balance += curAmount
                }
                val prevBalance = walletDao.getWalletByAddress(wallet.address).get(0).balance
                val newBalance = balance / division
                VLog.d("Updating balance for wallet : ${wallet.fingerPrint} from $prevBalance to balance : $newBalance and newStartHeight : $newStartHeight")
                walletDao.updateWalletBalanceByAddress(newBalance, wallet.address)
            } else {
                VLog.d("Request is not success in updating balance : ${request.message()}")
            }
        } catch (ex: Exception) {
            VLog.d("Exception occurred in updating wallet balance : ${ex.message}")
        }
    }

    override suspend fun updateWalletBalanceWithTransactions(wallet: WalletEntity) {
        try {
            val networkItem = getNetworkItemFromPrefs(wallet.networkType)
                ?: throw Exception("Exception in converting json str to networkItem")

            val curBlockChainService = retrofitBuilder.baseUrl(networkItem.full_node + "/").build()
                .create(BlockChainService::class.java)
            val tokensStartHeight = wallet.tokensStartHeight
            val prevStartHeight = tokensStartHeight[getShortNetworkType(wallet.networkType)] ?: 0
            val body = hashMapOf<String, Any>()
            body["puzzle_hashes"] = wallet.puzzle_hashes
            body["include_spent_coins"] = false
            val division = if (isThisChivesNetwork(wallet.networkType)) Math.pow(
                10.0, 8.0
            ) else Math.pow(
                10.0, 12.0
            )
            var balance = 0L
            val request = curBlockChainService.queryBalanceByPuzzleHashes(body)
            var newStartHeight = Long.MAX_VALUE
            if (request.isSuccessful) {
//				VLog.d("Got coin records : ${request.body()!!.coin_records}")
                for (coin in request.body()!!.coin_records) {
                    val curAmount = coin.coin.amount
                    val spent = coin.spent
                    newStartHeight = Math.min(newStartHeight, coin.confirmed_block_index)
                    if (!spent) balance += curAmount
                }
                val prevBalance = walletDao.getWalletByAddress(wallet.address).get(0).balance
                val newBalance = balance / division
                VLog.d("Updating balance for wallet : ${wallet.fingerPrint} from $prevBalance to balance : $newBalance and newStartHeight : $newStartHeight")
                if (prevBalance != newBalance) {
                    VLog.d("CurBalance is not the same as the previous one for wallet : ${wallet.fingerPrint}")
                    tokensStartHeight[getShortNetworkType(wallet.networkType)] = newStartHeight
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

    override suspend fun push_tx_nft(
        jsonSpendBundle: String,
        url: String,
        destPuzzleHash: String,
        nftInfo: NFTInfo,
        spentCoinsJson: String,
        fee: Double,
        confirmHeight: Int,
        networkType: String
    ): Resource<String> {
        try {
            val serverTime = greenAppInteract.getServerTime()
            if (serverTime == -1L) throw ServerMaintenanceExceptions()
            val timeBeforePushingTrans = serverTime - 60 * 1000
            val curBlockChainService =
                retrofitBuilder.baseUrl("$url/").build().create(BlockChainService::class.java)
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
                val coin = CoinDto(amount, parent_coin_info, puzzle_hash)
                val coinSpend = CoinSpend(coin, puzzle_reveal, solution)
                coinSpends.add(coinSpend)
            }
            val spendBundle = SpendBundle(agg_signature, coinSpends.toList())
            val spenBundle = SpenBunde(spendBundle)
            VLog.d("SpendBundle Sending to server push_tx : $spenBundle")
            val res = curBlockChainService.pushTransaction(spenBundle)
            if (res.isSuccessful) {
                val status = res.body()!!.status
                if (status == "SUCCESS") {

                    val trans = TransactionEntity(
                        UUID.randomUUID().toString(),
                        1.0,
                        timeBeforePushingTrans,
                        0,
                        Status.InProgress,
                        networkType,
                        destPuzzleHash,
                        fkAddress = nftInfo.fk_address,
                        fee,
                        "NFT",
                        confirmHeight,
                        nft_coin_hash = nftInfo.nft_coin_hash
                    )
                    val inserted = transactionDao.insertTransaction(trans)
                    VLog.d("Inserting transaction after pushing : $trans, Inserted : $inserted")
                    spentCoinsInteract.insertSpentCoinsJson(
                        spentCoinsJson,
                        timeBeforePushingTrans,
                        "XCH",
                        nftInfo.fk_address
                    )
                    nftInfoDao.updateIsPendingNFTInfoByNFTCoinHash(true, nftInfo.nft_coin_hash)

                    return Resource.success("OK")
                }
            }
        } catch (ex: Exception) {
            VLog.d("Exception in push tx nft : $ex")
        }
        return Resource.error(Exception("No"))
    }

    suspend fun updateIncomingTransactions(wallet: WalletEntity) {
        try {
            val networkItem = getNetworkItemFromPrefs(wallet.networkType)
                ?: throw Exception("Exception in converting json str to networkItem")

            val curBlockChainService = retrofitBuilder.baseUrl(networkItem.full_node + "/").build()
                .create(BlockChainService::class.java)
            val body = hashMapOf<String, Any>()
            body["puzzle_hashes"] = wallet.puzzle_hashes
            body["include_spent_coins"] = false
            val division = if (isThisChivesNetwork(wallet.networkType)) Math.pow(
                10.0, 8.0
            ) else Math.pow(
                10.0, 12.0
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
                        parent_coin_info, curBlockChainService
                    )!!["puzzle_hash"] as String
                    val transExistByParentInfo =
                        transactionDao.checkTransactionByIDExistInDB(parent_coin_info)
                    if (timeStamp * 1000 >= wallet.savedTime && (!wallet.puzzle_hashes.contains(
                            parent_puzzle_hash.substring(
                                2
                            )
                        ) || record.coinbase) && !transExistByParentInfo.isPresent
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
                            getShortNetworkType(wallet.networkType),
                            0
                        )
                        val formatted = formattedDoubleAmountWithPrecision(coinAmount / division)
                        val resLanguageResource =
                            prefs.getSettingString(PrefsManager.LANGUAGE_RESOURCE, "")
                        if (resLanguageResource.isEmpty()) return
                        val resMap = Converters.stringToHashMap(resLanguageResource)
                        val incoming_transaction =
                            resMap["push_notifications_incoming"] ?: "Incoming transaction"
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
        parent_info: String, service: BlockChainService
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

    override suspend fun getNftInfoByCoinID(
        networkType: String,
        coinID: String
    ): NftOfferCoin? {
        val networkItem = getNetworkItemFromPrefs(networkType)
            ?: throw Exception("Exception in converting json str to networkItem")
        val walletService = retrofitBuilder.baseUrl(networkItem.wallet + '/').build()
            .create(BlockChainService::class.java)
        val body = hashMapOf<String, Any>()
        body["coin_id"] = coinID
        body["latest"] = true
        body["ignore_size_limit"] = false
        body["reuse_puzhash"] = true
        val reqNFTInfo = walletService.getNFTInfoByCoinId(body)
        if (reqNFTInfo.isSuccessful) {
            val nftInfo = reqNFTInfo.body()!!.nft_info
            val metaData = getMetaDataNFT(nftInfo.metadata_uris?.get(0) ?: "") ?: return null
            val collection = metaData["collection"].toString()
            return NftOfferCoin(
                imageUrl = nftInfo.data_uris?.get(0) ?: "",
                collection = collection,
                nftID = nftInfo.nft_id ?: "",
            )
        }
        return null
    }
}
