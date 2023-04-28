package com.green.wallet.domain.interact

import com.green.wallet.domain.domainmodel.WalletWithNFT
import kotlinx.coroutines.flow.Flow

interface NFTInteract {

	suspend fun getHomeAddedWalletWithNFTTokensFlow(): Flow<List<WalletWithNFT>>


}
