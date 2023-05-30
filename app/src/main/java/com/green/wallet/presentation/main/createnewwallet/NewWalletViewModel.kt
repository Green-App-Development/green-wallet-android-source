package com.green.wallet.presentation.main.createnewwallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.green.wallet.domain.domainmodel.Wallet
import com.green.wallet.domain.interact.BlockChainInteract
import com.green.wallet.domain.interact.CryptocurrencyInteract
import com.green.wallet.domain.interact.GreenAppInteract
import com.green.wallet.domain.interact.TokenInteract
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class NewWalletViewModel @Inject constructor(
	private val blockChainInteract: BlockChainInteract,
	private val greenAppInteract: GreenAppInteract,
	private val tokenInteract: TokenInteract,
	private val cryptocurrencyInteract: CryptocurrencyInteract
) :
	ViewModel() {

	init {

	}

	private val handler = CoroutineExceptionHandler { _, thr ->

	}

	fun createNewWallet(
		wallet: Wallet,
		callBack: () -> Unit
	) = viewModelScope.launch(handler) {
		blockChainInteract.saveNewWallet(
			wallet = wallet,
			imported = false
		)
		callBack()
	}

	suspend fun getTokenDefaultOnMainScreen() = tokenInteract.getTokenListDefaultOnMainScreen()

	suspend fun getCoinDetails(coinCode: String) = greenAppInteract.getCoinDetails(coinCode)

}
