package com.green.wallet.presentation.main.send

import androidx.lifecycle.viewModelScope
import com.green.wallet.domain.domainmodel.Address
import com.green.wallet.domain.domainmodel.WalletWithTokens
import com.green.wallet.domain.interact.*
import com.green.wallet.presentation.tools.Resource
import com.green.wallet.presentation.tools.VLog
import com.greenwallet.core.base.BaseViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


class SendFragmentViewModel @Inject constructor(
    private val walletInteract: WalletInteract,
    private val blockChainInteract: BlockChainInteract,
    private val addressInteract: AddressInteract,
    private val greenAppInteract: GreenAppInteract,
    private val tokenInteract: TokenInteract,
    private val spentCoinsInteract: SpentCoinsInteract,
    private val dexieInteract: DexieInteract
) : BaseViewModel<TransferState, TransferEvent>(TransferState()) {

    init {
        getDexieFee()
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
            VLog.d("Dexie Recommended Fee : $dexie")
            _viewState.update {
                it.copy(
                    dexieFee = dexie.recommended
                )
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        _viewState.update { it.copy(isLoading = loading) }
    }

    fun updateSpendableBalance(wallet: WalletWithTokens) {
        viewModelScope.launch {
            spentCoinsInteract.getSpentCoinsBalanceByAddressAndCode(wallet.address, "XCH")
                .collect { amount ->
                    _viewState.update { it.copy(xchSpendableBalance = wallet.tokenWalletList[0].amount - amount) }
                }
        }
    }

    fun updateChosenFee(fee: Double) {
        viewModelScope.launch {
            _viewState.update { it.copy(chosenFee = fee) }
            validatingEnoughAmounts()
        }
    }

    fun updateSendingToken(posAdapter: Int) {
        _viewState.update { it.copy(xchSending = posAdapter == 0) }
    }

    fun updateSendingAmount(amountStr: String) {
        //1.
        val amount = amountStr.toDoubleOrNull()
        if (amount == null) {
            _viewState.update { it.copy(amountValid = false) }
        } else {
            _viewState.update { it.copy(sendingAmount = amount, amountValid = true) }
        }
    }

    private fun validatingEnoughAmounts() {
        val value = viewState.value
        if (viewState.value.xchSending) {
            val totalAmount = value.sendingAmount + value.chosenFee
            val isEnough = totalAmount <= value.xchSpendableBalance
            _viewState.update { it.copy(isSendingAmountEnough = isEnough, isFeeEnough = isEnough) }
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

}
