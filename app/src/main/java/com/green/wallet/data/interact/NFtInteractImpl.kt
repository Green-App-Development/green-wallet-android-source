package com.green.wallet.data.interact

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.green.wallet.data.local.NftCoinsDao
import com.green.wallet.data.local.NftInfoDao
import com.green.wallet.data.local.WalletDao
import com.green.wallet.data.preference.PrefsManager
import com.green.wallet.domain.domainmodel.NFTCoin
import com.green.wallet.domain.domainmodel.NFTInfo
import com.green.wallet.domain.domainmodel.WalletWithNFTInfo
import com.green.wallet.domain.interact.NFTInteract
import com.green.wallet.domain.interact.PrefsInteract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NFtInteractImpl @Inject constructor(
    private val walletDao: WalletDao,
    private val nftInfoDao: NftInfoDao,
    private val nftCoinDao: NftCoinsDao,
    private val prefs: PrefsInteract
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
        if(nftCoin.isPresent)
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


}
