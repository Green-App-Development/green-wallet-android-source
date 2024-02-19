package com.green.wallet.domain.interact

import com.green.wallet.data.network.dto.greenapp.network.NetworkItem
import com.green.wallet.domain.domainmodel.NFTCoin
import com.green.wallet.domain.domainmodel.NFTInfo
import com.green.wallet.domain.domainmodel.WalletWithNFTInfo
import kotlinx.coroutines.flow.Flow

interface NFTInteract {

    fun getHomeAddedWalletWithNFTTokensFlow(): Flow<List<WalletWithNFTInfo>>
    suspend fun getNFTCoinByHash(coinHash: String): NFTCoin?

    suspend fun getNftINFOByHash(nftCoinHash: String): NFTInfo

    suspend fun updateNftInfoPending(pending: Boolean, nftHash: String)

    suspend fun getCollectionNameByNFTID(networkItem: NetworkItem, nftID: String): String

    suspend fun getNFTCoinHashByNFTID(nftID: String): String

    suspend fun getNFTCoinByNFTIDFromWallet(networkItem: NetworkItem,nftID: String): NFTCoin?

}
