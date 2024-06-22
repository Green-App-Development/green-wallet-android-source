package com.green.wallet.presentation.main.send

import androidx.lifecycle.viewModelScope
import com.green.wallet.domain.domainmodel.Address
import com.green.wallet.domain.domainmodel.WalletWithTokens
import com.green.wallet.domain.interact.AddressInteract
import com.green.wallet.domain.interact.BlockChainInteract
import com.green.wallet.domain.interact.DexieInteract
import com.green.wallet.domain.interact.GreenAppInteract
import com.green.wallet.domain.interact.NamesXCHDaoInteract
import com.green.wallet.domain.interact.SpentCoinsInteract
import com.green.wallet.domain.interact.TokenInteract
import com.green.wallet.domain.interact.WalletInteract
import com.green.wallet.presentation.main.pincode.PinCodeCommunicator
import com.green.wallet.presentation.tools.ReasonEnterCode
import com.green.wallet.presentation.tools.Resource
import com.green.wallet.presentation.tools.VLog
import com.greenwallet.core.base.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


class SendViewModel @Inject constructor(
    private val walletInteract: WalletInteract,
    private val blockChainInteract: BlockChainInteract,
    private val addressInteract: AddressInteract,
    private val greenAppInteract: GreenAppInteract,
    private val tokenInteract: TokenInteract,
    private val spentCoinsInteract: SpentCoinsInteract,
    private val dexieInteract: DexieInteract,
    private val pinCodeCommunicator: PinCodeCommunicator,
    private val namesXCHDaoInteract: NamesXCHDaoInteract
) : BaseViewModel<TransferState, TransferEvent>(TransferState()) {

    init {
//        VLog.d("SendFragmentViewModel memory location :  $this")
        getDexieFee()

        pinCodeCommunicator.onSuccessPassCode = {
            when (it) {
                ReasonEnterCode.SEND_TRANSACTION -> {
                    setEvent(TransferEvent.OnPinConfirmed)
                }

                else -> Unit
            }
        }
    }

    suspend fun getDistinctNetworkTypeValues() = walletInteract.getDistinctNetworkTypes()

    fun queryWalletWithTokensList(type: String, fingerPrint: Long?) =
        walletInteract.getWalletWithTokensByFingerPrintNetworkTypeFlow(fingerPrint, type)

    fun pushTransaction(
        spendBundle: String,
        url: String,
        amount: Double,
        networkType: String,
        fingerPrint: Long,
        code: String,
        destPuzzleHash: String,
        address: String,
        fee: Double,
        spentCoinsJson: String,
        spentCoinsToken: String
    ) {
        viewModelScope.launch {
            setLoading(true)
            val result = blockChainInteract.push_tx(
                spendBundle,
                url,
                amount,
                networkType,
                fingerPrint,
                code,
                destPuzzleHash,
                address,
                fee,
                spentCoinsJson,
                spentCoinsToken
            )

            when (result.state) {
                Resource.State.SUCCESS -> {
                    setLoading(false)
                    setEvent(TransferEvent.OnSuccessTransfer)
                }

                Resource.State.ERROR -> {
                    setLoading(false)
                    setEvent(TransferEvent.OnErrorTransfer)
                }

                Resource.State.LOADING -> Unit
            }
        }
    }

    suspend fun checkIfAddressExistInDb(address: String) =
        addressInteract.checkIfAddressAlreadyExist(address = address)

    suspend fun getCoinDetailsFeeCommission(code: String) =
        greenAppInteract.getCoinDetails(code).fee_commission


    suspend fun getTokenPriceByCode(code: String) = tokenInteract.getTokenPriceByCode(code)

    suspend fun insertAddressEntity(address: Address) = addressInteract.insertAddressEntity(address)

    fun getSpentCoinsAmountsAddressCodeForSpendableBalance(
        address: String, tokenCode: String, networkType: String
    ) = spentCoinsInteract.getSumSpentCoinsForSpendableBalance(networkType, address, tokenCode)


    suspend fun swipedRefreshLayout(onFinished: () -> Unit) {
        blockChainInteract.updateBalanceAndTransactionsPeriodically()
        greenAppInteract.requestOtherNotifItems()
        onFinished.invoke()
    }

    suspend fun getSpentCoinsToPushTrans(networkType: String, address: String, tokenCode: String) =
        spentCoinsInteract.getSpentCoinsToPushTrans(networkType, address, tokenCode)

    private fun getDexieFee() {
        viewModelScope.launch {
            val dexie = dexieInteract.getDexieMinFee()
            _viewState.update {
                it.copy(
                    dexieFee = dexie.recommended
                )
            }
            validatingEnoughAmounts()
        }
    }

    fun setLoading(loading: Boolean) {
        _viewState.update { it.copy(isLoading = loading) }
    }

    private var xchJob: Job? = null

    fun updateSpendableBalance(wallet: WalletWithTokens) {
        xchJob?.cancel()
        xchJob = viewModelScope.launch {
            spentCoinsInteract.getSpentCoinsBalanceByAddressAndCode(wallet.address, "XCH")
                .collect { amount ->
//                    VLog.d("Wallet's With Amount : ${wallet.tokenWalletList[0].amount} on update spendable balance")
                    _viewState.update {
                        it.copy(
                            xchSpendableBalance = Math.max(
                                0.0,
                                wallet.tokenWalletList[0].amount - amount
                            )
                        )
                    }
                    validatingEnoughAmounts()
                }
        }
    }

    fun updateChosenFee(fee: Double) {
//        VLog.d("Chosen Fee on Send VM : $fee")
        viewModelScope.launch {
            _viewState.update { it.copy(chosenFee = fee) }
            validatingEnoughAmounts()
        }
    }

    fun updateSendingToken(posAdapter: Int) {
        _viewState.update { it.copy(xchSending = posAdapter == 0) }
        validatingEnoughAmounts()
    }

    fun updateSendingAmount(amountStr: String) {
        val amount = amountStr.toDoubleOrNull()
        VLog.d("Amount String Sending : $amount")
        if (amount == 0.0 || amount == null) {
            _viewState.update { it.copy(sendingAmount = 0.0, amountValid = false) }
        } else {
            _viewState.update { it.copy(sendingAmount = amount, amountValid = true) }
        }
        validatingEnoughAmounts()
    }

    fun updateSendingAddress(address: String) {
        _viewState.update { it.copy(destAddress = address) }
        validatingEnoughAmounts()
        lookUpNamesDao()
    }

    private fun lookUpNamesDao() {
        val address = viewState.value.destAddress
        if (address.endsWith(".xch")) {
            viewModelScope.launch {
                val result =
                    namesXCHDaoInteract.getNamesXCHAddress(address.removeSuffix(".xch").trim())
                _viewState.update {
                    it.copy(
                        namesDao = result
                    )
                }
            }
        } else {
            _viewState.update {
                it.copy(
                    namesDao = null
                )
            }
        }
    }

    fun updateCATSpendableBalance(amount: Double) {
        _viewState.update { it.copy(catSpendableAmount = amount) }
        validatingEnoughAmounts()
    }

    private fun validatingEnoughAmounts() {
        val value = viewState.value
        if (viewState.value.xchSending) {
//            Timber.d("Sending Amount : ${value.sendingAmount}  Choose Fee : ${value.chosenFee}")
            val totalAmount = value.sendingAmount + value.chosenFee
            val isEnough = totalAmount <= value.xchSpendableBalance
            _viewState.update { it.copy(isSendingAmountEnough = isEnough, isFeeEnough = isEnough) }
//            Timber.d("is enough sending view state : ${viewState.value}")
        } else {
            val sendEnough = value.sendingAmount <= value.catSpendableAmount
            val feeEnough = value.chosenFee <= value.xchSpendableBalance
            _viewState.update {
                it.copy(
                    isSendingAmountEnough = sendEnough,
                    isFeeEnough = feeEnough
                )
            }
        }
    }

    override fun onCleared() {
        VLog.d("On Cleared on send view model : ${viewState.value}")
        super.onCleared()
    }

}
