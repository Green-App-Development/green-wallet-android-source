package com.green.wallet.presentation.main.swap.exchange

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.green.wallet.domain.domainmodel.ChiaWallet
import com.green.wallet.domain.domainmodel.ExchangeInput
import com.green.wallet.domain.domainmodel.ExchangeRate
import com.green.wallet.domain.interact.ExchangeInteract
import com.green.wallet.domain.interact.WalletInteract
import com.green.wallet.presentation.tools.INPUT_DEBOUNCE_VALUE
import com.green.wallet.presentation.tools.Resource
import com.green.wallet.presentation.tools.VLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

class ExchangeViewModel @Inject constructor(
    private val walletInteract: WalletInteract, private val exchangeInteract: ExchangeInteract
) : ViewModel() {

    private val _chiaWalletList = MutableStateFlow<List<ChiaWallet>?>(null)
    val chiaWalletList = _chiaWalletList.asStateFlow()

    private val _exchangeRequest = MutableStateFlow<Resource<ExchangeRate>?>(Resource.loading())
    val exchangeRequest = _exchangeRequest.asStateFlow()

    private val handler = CoroutineExceptionHandler { _, th ->
        VLog.d("Exception caught in exchange view model : ${th.message}")
    }

    private val _outputExchange = MutableStateFlow<Resource<Double>?>(Resource.loading())
    val outputExchange = _outputExchange.asStateFlow()

    private val userSwapInputChannel = Channel<ExchangeInput>()


    var walletPosition = 0
    var tokenToSpinner = 1
    var rateConversion = 0.0
    var containerBigger = true


    init {
        VLog.d("Creating exchange view model : $this")
        retrieveChiaWallet()
    }


    suspend fun requestingOrder(
        amount: Double,
        get_address: String,
        get_coin: String,
        getAmount: Double,
        feeNetwork: Double,
        rate: Double
    ): Resource<String> {
        val giveAddress = exchangeRequest.value?.data?.give_address ?: return Resource.error(
            Exception(
                "No Give Address present"
            )
        )
        return exchangeInteract.createExchangeRequest(
            giveAddress,
            amount,
            get_address,
            get_coin,
            rate,
            getAmount,
            feeNetwork
        )
    }

    fun retrieveChiaWallet() {
        viewModelScope.launch {
            val res = walletInteract.getChiaWalletListForExchange()
            _chiaWalletList.emit(res)
        }
    }

    var requestJob: Job? = null
    var prevToken = ""
    fun requestExchangeRate(fromToken: String) {
        if (prevToken == fromToken)
            return
        prevToken = fromToken
        VLog.d("Call from view model to get exchange request rate : $fromToken")
        requestJob?.cancel()
        requestJob = viewModelScope.launch() {
            while (true) {
                val res = exchangeInteract.getExchangeRequest(fromToken)
                if (isActive) {
                    _exchangeRequest.emit(res)
                }
                delay(30 * 1000L)
            }
        }
    }

    fun changeToDefault() {
        prevToken = ""
        _exchangeRequest.value = null
        _outputExchange.value = null
    }

    private var swapMainJob: Job? = null

    fun startDebounceFlow() {
        swapMainJob?.cancel()
        val debouncedFlow = userSwapInputChannel.receiveAsFlow().debounce(INPUT_DEBOUNCE_VALUE)
        swapMainJob = CoroutineScope(Dispatchers.IO).launch {
            debouncedFlow.collectLatest { input ->
                val res = exchangeInteract.getOutputPrice(
                    input.giveCoin,
                    input.getCoin,
                    input.amount,
                    input.rate
                )
                _outputExchange.emit(res)
            }
        }
    }


    fun onInputSwapAmountChanged(input: ExchangeInput) = userSwapInputChannel.trySend(input)


    override fun onCleared() {
        super.onCleared()
        VLog.d("On cleared of exchange view model : $this")
    }

}
