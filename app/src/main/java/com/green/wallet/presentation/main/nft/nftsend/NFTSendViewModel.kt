package com.green.wallet.presentation.main.nft.nftsend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.green.wallet.domain.domainmodel.NFTCoin
import com.green.wallet.domain.domainmodel.NFTInfo
import com.green.wallet.domain.domainmodel.Wallet
import com.green.wallet.domain.interact.NFTInteract
import com.green.wallet.domain.interact.WalletInteract
import kotlinx.coroutines.launch
import javax.inject.Inject


class NFTSendViewModel @Inject constructor(
	private val nftInteract: NFTInteract,
	private val walletInteract: WalletInteract
) : ViewModel() {

	lateinit var wallet: Wallet
	lateinit var nftCoin: NFTCoin

	init {

	}

	fun initNFTCoin(nftInfo: NFTInfo) {
		viewModelScope.launch {
			wallet = walletInteract.getWalletByAddress(nftInfo.fk_address)
			nftCoin = nftInteract.getNFTCoinByHash(nftInfo.nft_coin_hash)
		}
	}

}
