package com.green.wallet.presentation.main.swap.exchange

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.green.wallet.domain.domainmodel.ChiaWallet
import com.green.wallet.domain.interact.WalletInteract
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ExchangeViewModel @Inject constructor(
	private val walletInteract: WalletInteract
) : ViewModel() {

	private val _chiaWalletList = MutableStateFlow<List<ChiaWallet>>(emptyList())
	val chiaWalletList = _chiaWalletList.asStateFlow()

	var walletPosition = 0

	init {
		retrieveChiaWallet()
	}

	fun retrieveChiaWallet() {
		viewModelScope.launch {
			val res = walletInteract.getChiaWalletListForExchange()
			_chiaWalletList.emit(res)
		}
	}


}
