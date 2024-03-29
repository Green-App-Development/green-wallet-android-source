package com.green.wallet.presentation.main.importtoken

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.green.wallet.domain.interact.CryptocurrencyInteract
import com.green.wallet.domain.interact.TokenInteract
import com.green.wallet.domain.interact.WalletInteract
import kotlinx.coroutines.*
import javax.inject.Inject


class ImportTokenViewModel @Inject constructor(
	private val tokenInteract: TokenInteract,
	private val walletInteract: WalletInteract,
	private val cryptocurrencyInteract: CryptocurrencyInteract
) :
	ViewModel() {

	init {
		viewModelScope.launch {
            cryptocurrencyInteract.getAllTails()
            cryptocurrencyInteract.updateTokensPrice()
		}
	}

	suspend fun getTokenListAndSearch(fingerPrint: Long, nameCode: String?) =
		tokenInteract.getTokenListAndSearchForWallet(fingerPrint, nameCode)

	suspend fun getWalletByAddress(address: String) = walletInteract.getWalletByAddress(address)

	private var importTokenJob: Job? = null

	fun importToken(
		hash: String,
		address: String,
		add: Boolean,
		outer_puzzle_hash: List<String>,
		onFinishUpdating: () -> Unit
	) {
		importTokenJob?.cancel()
		importTokenJob = viewModelScope.launch {
			walletInteract.importTokenByAddress(address, add, hash, outer_puzzle_hash)
			onFinishUpdating()
		}
	}


}
