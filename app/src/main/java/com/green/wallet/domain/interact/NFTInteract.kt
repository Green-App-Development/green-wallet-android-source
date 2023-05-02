package com.green.wallet.domain.interact

import com.green.wallet.domain.domainmodel.WalletWithNFTInfo
import kotlinx.coroutines.flow.Flow

interface NFTInteract {

    fun getHomeAddedWalletWithNFTTokensFlow(): Flow<List<WalletWithNFTInfo>>


}
