package com.green.wallet.data.interact

import com.green.wallet.data.local.WalletDao
import com.green.wallet.domain.domainmodel.WalletWithNFT
import com.green.wallet.domain.interact.NFTInteract
import com.green.wallet.presentation.tools.VLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NFtInteractImpl @Inject constructor(
	private val walletDao: WalletDao
) : NFTInteract {


	override suspend fun getHomeAddedWalletWithNFTTokensFlow(): Flow<List<WalletWithNFT>> {
		val walletFlow = walletDao.getFLowOfWalletListWithNFTCoins()
		walletFlow.collectLatest {
			VLog.d("Wallet With NFT : $it and its hash : ${it[0].non_observer_hash}")
		}
		return flow {

		}
	}


}
