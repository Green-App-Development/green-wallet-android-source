package com.green.wallet.presentation.main.nft.usernfts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.green.wallet.domain.interact.BlockChainInteract
import com.green.wallet.domain.interact.NFTInteract
import kotlinx.coroutines.launch
import javax.inject.Inject


class UserNFTTokensViewModel @Inject constructor(
	private val nftInteract: NFTInteract,
	private val blockChainInteract: BlockChainInteract
) : ViewModel() {


	fun swipedRefreshClicked(onFinished: () -> Unit) {
		viewModelScope.launch {
			blockChainInteract.updateBalanceAndTransactionsPeriodically()
			onFinished()
		}
	}

	fun getHomeAddedWalletWithNFT() = nftInteract.getHomeAddedWalletWithNFTTokensFlow()

}
