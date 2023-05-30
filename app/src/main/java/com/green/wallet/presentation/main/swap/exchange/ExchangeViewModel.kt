package com.green.wallet.presentation.main.swap.exchange

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.green.wallet.domain.domainmodel.ChiaWallet
import com.green.wallet.domain.domainmodel.ExchangeRate
import com.green.wallet.domain.interact.ExchangeInteract
import com.green.wallet.domain.interact.WalletInteract
import com.green.wallet.presentation.tools.Resource
import com.green.wallet.presentation.tools.VLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

class ExchangeViewModel @Inject constructor(
	private val walletInteract: WalletInteract,
	private val exchangeInteract: ExchangeInteract
) : ViewModel() {

	private val _chiaWalletList = MutableStateFlow<List<ChiaWallet>>(emptyList())
	val chiaWalletList = _chiaWalletList.asStateFlow()

	private val _exchangeRequest = MutableStateFlow<Resource<ExchangeRate>?>(Resource.loading())
	val exchangeRequest = _exchangeRequest.asStateFlow()

	var walletPosition = 0
	var tokenToSpinner = 1

	init {
		retrieveChiaWallet()
	}

	fun retrieveChiaWallet() {
		viewModelScope.launch {
			val res = walletInteract.getChiaWalletListForExchange()
			_chiaWalletList.emit(res)
		}
	}

	var requestJob: Job? = null
	var prevFromToken = ""
	fun requestExchangeRate(fromToken: String) {
		if (prevFromToken == fromToken)
			return
		prevFromToken = fromToken
		VLog.d("Call from view model to get exchange request rate : $fromToken")
		requestJob?.cancel()
		requestJob = viewModelScope.launch() {
			val res = exchangeInteract.getExchangeRequest(fromToken)
			if (isActive)
				_exchangeRequest.emit(res)
		}
	}


}
