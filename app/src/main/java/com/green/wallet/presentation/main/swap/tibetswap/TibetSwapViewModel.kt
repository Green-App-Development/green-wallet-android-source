package com.green.wallet.presentation.main.swap.tibetswap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.green.wallet.domain.domainmodel.TibetSwap
import com.green.wallet.domain.domainmodel.Token
import com.green.wallet.domain.domainmodel.Wallet
import com.green.wallet.domain.interact.TokenInteract
import com.green.wallet.domain.interact.WalletInteract
import com.green.wallet.domain.usecases.TibetSwapUseCases
import com.green.wallet.presentation.tools.INPUT_DEBOUNCE_VALUE
import com.green.wallet.presentation.tools.Resource
import com.green.wallet.presentation.tools.VLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class TibetSwapViewModel @Inject constructor(
	private val tokenInteract: TokenInteract,
	private val tibetSwapUseCases: TibetSwapUseCases,
	private val walletInteract: WalletInteract
) : ViewModel() {

	var isShowingSwap = true

	var xchToCAT = true
	var nextHeight = 435
	val containerBiggerSize = 435
	val containerSmallerSize = 265
	var nextContainerBigger = true
	var catAdapPosition = 0
	var toTibet: Boolean = false

	var curWallet: Wallet? = null

	private val _tokenList = MutableStateFlow<List<Token>>(emptyList())
	val tokenList = _tokenList.asStateFlow()

	private val _tibetSwap = MutableStateFlow<Resource<TibetSwap>?>(null)
	val tibetSwap = _tibetSwap.asStateFlow()

	private val _walletList = MutableStateFlow<List<Wallet>>(emptyList())
	val walletList = _walletList.asStateFlow()


	private val userSwapInputChannel = Channel<Double>()

	var swapInputState = ""

	init {
		VLog.d("On create tibet vm : $this")
		retrieveTokenList()
		initWalletList()
	}

	private fun initWalletList() {
		viewModelScope.launch {
			val res = walletInteract.getAllWalletListFirstHomeIsAddedThenRemain()
			_walletList.emit(res)
		}
	}

	fun onInputSwapAmountChanged(amount: Double) = userSwapInputChannel.trySend(amount)

	private var swapMainScope: CoroutineScope? = null
	fun startDebounceValueSwap() {
		swapMainScope?.cancel()
		swapMainScope = CoroutineScope(Dispatchers.Main)
		val debouncedFlow = userSwapInputChannel.receiveAsFlow().debounce(INPUT_DEBOUNCE_VALUE)
		swapMainScope?.launch {
			debouncedFlow.collectLatest { input ->
				calculateAmountOut(input, _tokenList.value[catAdapPosition].pairID, xchToCAT)
			}
		}
	}

	suspend fun calculateAmountOut(amountIn: Double, pairID: String, isXCH: Boolean) {
		val res = tibetSwapUseCases.calculateAmountOut(amountIn, isXCH, pairID)
		VLog.d("Making API request : $amountIn PairID : $pairID  isXCH : $isXCH with Request : ${res.state}")
		_tibetSwap.emit(res)
	}

	private fun retrieveTokenList() {
		viewModelScope.launch {
			val res = tokenInteract.getTokenListPairIDExist()
			_tokenList.emit(res)
		}
	}

	override fun onCleared() {
		super.onCleared()
		VLog.d("On cleared tibet vm and cancelled scope : $this")
		swapMainScope?.cancel()
	}

}
