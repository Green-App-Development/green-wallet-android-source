package com.green.wallet.domain.interact

import com.green.wallet.domain.domainmodel.NFTCoin
import com.green.wallet.domain.domainmodel.NFTInfo
import com.green.wallet.domain.domainmodel.WalletWithNFTInfo
import kotlinx.coroutines.flow.Flow

interface NFTInteract {

	fun getHomeAddedWalletWithNFTTokensFlow(): Flow<List<WalletWithNFTInfo>>
	suspend fun getNFTCoinByHash(coinHash: String): NFTCoin

	suspend fun getNftINFOByHash(nftCoinHash: String): NFTInfo

	suspend fun updateNftInfoPending(pending:Boolean,nftHash:String)

}
