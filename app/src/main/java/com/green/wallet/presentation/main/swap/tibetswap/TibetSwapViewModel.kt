package com.green.wallet.presentation.main.swap.tibetswap

import androidx.lifecycle.viewModelScope
import com.green.wallet.data.preference.PrefsManager
import com.green.wallet.domain.domainmodel.TibetLiquidity
import com.green.wallet.domain.domainmodel.TibetLiquidityExchange
import com.green.wallet.domain.domainmodel.TibetSwapResponse
import com.green.wallet.domain.domainmodel.Token
import com.green.wallet.domain.domainmodel.Wallet
import com.green.wallet.domain.interact.DexieInteract
import com.green.wallet.domain.interact.SpentCoinsInteract
import com.green.wallet.domain.interact.TibetInteract
import com.green.wallet.domain.interact.TokenInteract
import com.green.wallet.domain.interact.WalletInteract
import com.green.wallet.domain.usecases.tibet.CheckingCATOnHome
import com.green.wallet.domain.usecases.tibet.TibetSwapUseCases
import com.green.wallet.presentation.tools.INPUT_DEBOUNCE_VALUE
import com.green.wallet.presentation.tools.Resource
import com.green.wallet.presentation.tools.VLog
import com.greenwallet.core.base.BaseViewModel
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import javax.inject.Inject

class TibetSwapViewModel @Inject constructor(
    private val tokenInteract: TokenInteract,
    private val tibetSwapUseCases: TibetSwapUseCases,
    private val walletInteract: WalletInteract,
    val prefsManager: PrefsManager,
    private val spentCoinsInteract: SpentCoinsInteract,
    val checkingCATHome: CheckingCATOnHome,
    private val tibetInteract: TibetInteract,
    private val dexieInteract: DexieInteract
) : BaseViewModel<TibetSwapState, Unit>(TibetSwapState()) {

    private val _viewStateLiquidity = MutableStateFlow(TibetLiquidityViewState())
    val viewStateLiquidity = _viewStateLiquidity.asStateFlow()

    var isShowingSwap = true

    var xchToCAT = true
    var isPriceImpacted = false

    //	val containerBiggerSize = if (isPriceImpacted) 480 else 435
//	val containerSmallerSize = if (isPriceImpacted) 310 else 265

    var nextContainerBigger = true
    var catAdapPosition = 0
    var catTibetAdapterPosition = -1
    var catLiquidityAdapterPos = -1
    var toTibet: Boolean = true

    var curWallet: Wallet? = null
    var curTibetLiquidity: TibetLiquidityExchange? = null

    var xchDeposit: Double = 0.0
    var catTibetAmount: Double = 0.0
    var liquidityAmount: Double = 0.0
    var availableXCHAmount: Double = 0.0
    var catEnough = false

    private val _tokenList = MutableStateFlow<List<Token>?>(null)
    val tokenList = _tokenList.asStateFlow()

    private val _tokenTibetList = MutableStateFlow<List<Token>?>(null)
    val tokenTibetList = _tokenTibetList.asStateFlow()


    private val _tibetSwap = MutableStateFlow<Resource<TibetSwapResponse>?>(null)
    val tibetSwap = _tibetSwap.asStateFlow()

    private val _walletList = MutableStateFlow<List<Wallet>?>(null)
    val walletList = _walletList.asStateFlow()

    private val _pushingOfferTibet = MutableStateFlow<Resource<String>?>(null)
    val pushingOfferTibet = _pushingOfferTibet.asStateFlow()

    private val userSwapInputChannel = Channel<Double>()

    var swapInputState = ""

    private val handler = CoroutineExceptionHandler { context, throwable ->
        VLog.d("Exception in tibet swap view model : $throwable")
    }

    var onSuccessTibetSwapClearingFields: () -> Unit = {}

    var onSuccessTibetLiquidityClearingFields: () -> Unit = {}

    init {
        VLog.d("On create tibet vm swap : $this")
        getDexie()
    }

    private fun getDexie() {
        viewModelScope.launch {
            val dexie = dexieInteract.getDexieMinFee().recommended
            _viewState.update { it.copy(dexieFee = dexie) }
            _viewStateLiquidity.update { it.copy(dexieFee = dexie) }
        }
    }

    fun getContainerBiggerSize(): Int {
        return if (isPriceImpacted) 480 else 435
    }

    fun getContainerSmallerSize(): Int {
        return if (isPriceImpacted) 315 else 265
    }

    fun getNextHeightSize(): Int {
        return if (nextContainerBigger) getContainerBiggerSize() else getContainerSmallerSize()
    }

    fun retrieveTibetTokenList() {
        viewModelScope.launch {
            val res = tokenInteract.getTibetTokenList()
            val newList = mutableListOf<Token>()
            var gwtToken: Token? = null
            for (token in res) {
                if (token.code.contains("GWT-XCH")) {
                    gwtToken = token
                } else
                    newList.add(token)
            }
            if (gwtToken != null)
                newList.add(0, gwtToken)
            VLog.d("New List for tibet tokens : $newList ")
            _tokenTibetList.emit(newList)
        }
    }

    private var prevWalletListJob: Job? = null

    fun initWalletList() {
        prevWalletListJob?.cancel()
        prevWalletListJob = CoroutineScope(Dispatchers.IO).launch {
            val res = walletInteract.getAllWalletListFirstHomeIsAddedThenRemain()
            _walletList.emit(res)
        }
    }

    fun onInputSwapAmountChanged(amount: Double) = userSwapInputChannel.trySend(amount)

    var swapMainScope: CoroutineScope? = null
    fun startDebounceValueSwap() {
        swapMainScope?.cancel()
        swapMainScope = CoroutineScope(Dispatchers.Main.immediate)
        val debouncedFlow = userSwapInputChannel.receiveAsFlow().debounce(INPUT_DEBOUNCE_VALUE)
        swapMainScope?.launch(handler) {
            debouncedFlow.collectLatest { input ->
                calculateAmountOut(
                    input,
                    _tokenList.value?.get(catAdapPosition)?.pairID ?: "",
                    xchToCAT
                )
            }
        }
    }

    fun tibetSwapReInitToNullValue() {
        _tibetSwap.value = null
    }

    private suspend fun calculateAmountOut(amountIn: Double, pairID: String, isXCH: Boolean) {
        val res = tibetSwapUseCases.calculateAmountOut(amountIn, isXCH, pairID)
        VLog.d("Making API request : $amountIn PairID : $pairID  isXCH : $isXCH with Request : ${res.state}")
        _tibetSwap.emit(res)
    }

    suspend fun getSpendableBalanceByTokenCodeAndAddress(
        address: String,
        tokenCode: String,
        assetID: String
    ) =
        spentCoinsInteract.getSpendableBalanceByTokenCode(
            assetID = assetID,
            tokenCode = tokenCode,
            address = address
        )

    fun retrieveTokenList() {
        viewModelScope.launch {
            val res = tokenInteract.getTokenListPairIDExist()
            val newList = mutableListOf<Token>()
            var gwtToken: Token? = null
            for (token in res) {
                if (token.code == "GWT") {
                    gwtToken = token
                } else
                    newList.add(token)
            }
            if (gwtToken != null)
                newList.add(0, gwtToken)
            VLog.d("New List Token To Retrieve : $newList")
            _tokenList.emit(newList)
        }
    }

    suspend fun pushOfferCATXCHToTibet(
        pairID: String,
        offer: String,
        amountFrom: Double,
        amountTo: Double,
        catCode: String,
        isInputXCH: Boolean,
        fee: Double,
        spentXCHCoinsJson: String,
        spentCATCoinsJson: String,
        donationAmount: Double,
        devFee: Int,
        walletFee: Int
    ) =
        tibetSwapUseCases.pushOfferCATXCHToTibet(
            pairID,
            offer,
            amountFrom,
            amountTo,
            catCode,
            isInputXCH,
            fee,
            spentXCHCoinsJson,
            spentCATCoinsJson,
            fk_address = curWallet!!.address,
            donationAmount,
            devFee,
            walletFee
        )

    suspend fun pushingOfferXCHCATToTibet(
        pairID: String,
        offer: String,
        amountFrom: Double,
        amountTo: Double,
        catCode: String,
        isInputXCH: Boolean,
        fee: Double,
        spentXCHCoinsJson: String,
        donationAmount: Double,
        devFee: Int,
        walletFee: Int
    ) =
        tibetSwapUseCases.pushOfferXCHCATToTibet(
            pairID,
            offer,
            amountFrom,
            amountTo,
            catCode,
            isInputXCH,
            fee,
            spentXCHCoinsJson,
            curWallet!!.address,
            donationAmount,
            devFee,
            walletFee
        )

    suspend fun addLiquidity(
        offer: String,
        pairId: String,
        xchCoins: String,
        catCoins: String,
        tibetLiquid: TibetLiquidity
    ) =
        tibetSwapUseCases.addLiquidity(
            offer,
            pairId,
            xchCoins,
            catCoins,
            curWallet!!.address,
            tibetLiquid
        )

    suspend fun removeLiquidity(
        offer: String,
        pairId: String,
        liquidCoins: String,
        xchCoins: String,
        tibetLiquid: TibetLiquidity
    ) =
        tibetSwapUseCases.removeLiquidity(
            offer = offer,
            pairId = pairId,
            address = curWallet!!.address,
            liquidityCoins = liquidCoins,
            xchCoins = xchCoins,
            tibetLiquidity = tibetLiquid
        )

    suspend fun getTibetLiquidity(pairID: String) = tibetInteract.getTibetLiquidity(pairID)

    suspend fun getSpentCoinsToPushTrans(networkType: String, address: String, tokenCode: String) =
        spentCoinsInteract.getSpentCoinsToPushTrans(networkType, address, tokenCode)

    suspend fun fiveMinTillGetListOfTokensFromTibet() {
        while (tokenList.value == null || tokenList.value?.isEmpty() == true) {
            tibetInteract.saveTokensPairID()
            delay(5000L)
        }
    }

    fun calculateSpendableBalance(scope: CoroutineScope) {
        scope.launch {
            spentCoinsInteract.getSpentCoinsBalanceByAddressAndCode(
                curWallet?.address.orEmpty(),
                "XCH"
            ).collectLatest { amount ->
                val spendableBalance = (curWallet?.balance ?: 0.0) - amount
                VLog.d("Spendable Balance for wallet : $curWallet, Amount : $spendableBalance")
                _viewState.update { it.copy(spendableBalance = spendableBalance) }
                _viewStateLiquidity.update { it.copy(spendableBalance = spendableBalance) }
            }
        }
    }

    fun updateFeeChosen(fee: Double) {
        _viewState.update { it.copy(feeEnough = it.spendableBalance >= fee, fee = fee) }
    }

    fun updateFeeChosenLiquidity(fee: Double) {
        _viewStateLiquidity.update { it.copy(fee = fee) }
        validateFeeEnoughAmountsLiquidity()
    }

    fun validateFeeEnoughAmountsLiquidity() {
        var needXCH = _viewStateLiquidity.value.fee
        if (toTibet) {
            needXCH += xchDeposit
        }
        _viewStateLiquidity.update { it.copy(feeEnough = needXCH <= it.spendableBalance) }
    }

    override fun onCleared() {
        VLog.d("On cleared tibet vm and cancelled scope : $this")
        prevWalletListJob?.cancel()
    }

}
