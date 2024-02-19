package com.green.wallet.data.interact

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.green.wallet.data.local.NftCoinsDao
import com.green.wallet.data.local.NftInfoDao
import com.green.wallet.data.local.WalletDao
import com.green.wallet.data.network.BlockChainService
import com.green.wallet.data.network.dto.greenapp.network.NetworkItem
import com.green.wallet.data.preference.PrefsManager
import com.green.wallet.domain.domainmodel.NFTCoin
import com.green.wallet.domain.domainmodel.NFTInfo
import com.green.wallet.domain.domainmodel.WalletWithNFTInfo
import com.green.wallet.domain.interact.NFTInteract
import com.green.wallet.domain.interact.PrefsInteract
import com.green.wallet.presentation.tools.VLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.Retrofit
import javax.inject.Inject

class NFtInteractImpl @Inject constructor(
    private val walletDao: WalletDao,
    private val nftInfoDao: NftInfoDao,
    private val nftCoinDao: NftCoinsDao,
    private val prefs: PrefsInteract,
    private val retrofitBuilder: Retrofit.Builder,
) : NFTInteract {


    override fun getHomeAddedWalletWithNFTTokensFlow(): Flow<List<WalletWithNFTInfo>> {
        return walletDao.getFLowOfWalletListWithNFTCoins().map {

            var verifiedDID = mutableListOf<String>()
            val didListJson = prefs.getObjectString(PrefsManager.VERIFIED_DID_LIST)
            val type = object : TypeToken<MutableList<String>>() {}.type
            if (didListJson.isNotEmpty()) {
                verifiedDID = Gson().fromJson(didListJson, type)
            }
//			VLog.d("Final verified List : $verifiedDID")

            it.map {
                WalletWithNFTInfo(
                    it.fingerPrint,
                    it.address,
                    it.nftInfos.filter { !it.spent }
                        .map { it.toNFTInfo(verifiedDID.contains(it.minter_did.removePrefix("did:chia:"))) }
                )
            }
        }
    }

    override suspend fun getNFTCoinByHash(coinHash: String): NFTCoin? {
        val nftCoin = nftCoinDao.getNFTCoinByParentCoinInfo(coinHash)
        if (nftCoin.isPresent)
            return nftCoin.get().toNftCoin()
        return null
    }

    override suspend fun getNftINFOByHash(nftCoinHash: String): NFTInfo {
        val nftInfo = nftInfoDao.getNftInfoEntityByNftCoinHash(nftCoinHash)
        return nftInfo.get().toNFTInfo()
    }

    override suspend fun updateNftInfoPending(pending: Boolean, nftHash: String) {
        nftInfoDao.updateIsPendingNFTInfoByNFTCoinHash(pending, nftHash)
    }

    override suspend fun getCollectionNameByNFTID(networkItem: NetworkItem, nftID: String): String {
        try {
            val walletService = retrofitBuilder.baseUrl(networkItem.wallet + '/').build()
                .create(BlockChainService::class.java)
            val body = hashMapOf<String, Any>()
            body["coin_id"] = nftID
            body["latest"] = true
            body["ignore_size_limit"] = false
            body["reuse_puzhash"] = true
            val reqNFTInfo = walletService.getNFTInfoByCoinId(body)
            if (reqNFTInfo.isSuccessful) {
                val nftInfo = reqNFTInfo.body()!!.nft_info
                val metaData = getMetaDataNFT(nftInfo.metadata_uris?.get(0) ?: "")
                if (metaData == null) {
                    VLog.d("Meta Data of nft is null")
                    return ""
                }
                val description = metaData["description"].toString()
                val collection = metaData["collection"].toString()
                val name = metaData["name"].toString()
                return collection
            } else {
                VLog.d("Request is no success for nftInfo : ${reqNFTInfo.raw()}")
            }

        } catch (ex: Exception) {
            VLog.d("Exception occurred in getting nft info from wallet : ${ex.message}")
        }
        return ""
    }

    override suspend fun getNFTCoinHashByNFTID(nftID: String): String {
        val nftInfo = nftInfoDao.getNftInfoEntityByNftID(nftID)
        if (nftInfo.isPresent)
            return nftInfo.get().nft_coin_hash
        return ""
    }

    override suspend fun getNFTCoinByNFTIDFromWallet(
        networkItem: NetworkItem,
        nftID: String
    ): NFTCoin? {
        try {
            val walletService = retrofitBuilder.baseUrl(networkItem.wallet + '/').build()
                .create(BlockChainService::class.java)
            val body = hashMapOf<String, Any>()
            body["coin_id"] = nftID
            body["latest"] = true
            body["ignore_size_limit"] = false
            body["reuse_puzhash"] = true
            val reqNFTInfo = walletService.getNFTInfoByCoinId(body)
            if (reqNFTInfo.isSuccessful) {
                val nftInfo = reqNFTInfo.body()!!.nft_info
                val puzzleHash = nftInfo.p2_address ?: return null
                val nftCoinID = nftInfo.nft_coin_id ?: return null
                val parentCoinInfo =
                    getParentCoinInfoFromFullNode(nftCoinID, networkItem) ?: return null
                return NFTCoin(
                    coinInfo = parentCoinInfo,
                    addressFk = "",
                    coinHash = "",
                    1, 0, 0, 0,
                    puzzleHash
                )
            } else {
                VLog.d("Request is no success for nftInfo : ${reqNFTInfo.raw()}")
            }
        } catch (ex: Exception) {
            VLog.d("Exception in getNFTCoinByNFTIDFromWallet : ${ex.message}")
        }
        return null
    }

    private suspend fun getParentCoinInfoFromFullNode(
        nftCoinID: String,
        networkItem: NetworkItem
    ): String? {
        val service = retrofitBuilder.baseUrl(networkItem.full_node + '/').build()
            .create(BlockChainService::class.java)
        val body = hashMapOf<String, Any>()
        body["name"] = nftCoinID
        body["include_spent_coins"] = true
        val request = service.getCoinRecordByName(body)
        if (request.isSuccessful) {
            val coinRecordJson = request.body()!!["coin_record"].asJsonObject
            return coinRecordJson.get("coin").asJsonObject.get("parent_coin_info").asString
        } else {
            VLog.d("Request in getting parent coin info is not success : ${request.body()}")
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


}
