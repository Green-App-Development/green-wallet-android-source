package com.green.wallet.presentation.main.swap.send

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.green.wallet.domain.domainmodel.Address
import com.green.wallet.domain.domainmodel.Token
import com.green.wallet.domain.domainmodel.WalletWithTokens
import com.green.wallet.domain.interact.AddressInteract
import com.green.wallet.domain.interact.BlockChainInteract
import com.green.wallet.domain.interact.DexieInteract
import com.green.wallet.domain.interact.GreenAppInteract
import com.green.wallet.domain.interact.SpentCoinsInteract
import com.green.wallet.domain.interact.TokenInteract
import com.green.wallet.domain.interact.WalletInteract
import com.green.wallet.presentation.main.pincode.PinCodeCommunicator
import com.green.wallet.presentation.tools.ReasonEnterCode
import com.green.wallet.presentation.tools.VLog
import com.greenwallet.core.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class SwapSendViewModel @Inject constructor(
    private val walletInteract: WalletInteract,
    private val blockChainInteract: BlockChainInteract,
    private val addressInteract: AddressInteract,
    private val greenAppInteract: GreenAppInteract,
    private val tokenInteract: TokenInteract,
    private val spentCoinsInteract: SpentCoinsInteract,
    private val dexieInteract: DexieInteract,
    private val pinCodeCommunicator: PinCodeCommunicator
) : BaseViewModel<SwapSendViewState, SwapSendEvent>(SwapSendViewState()) {

    init {
        getDexieFee()
        pinCodeCommunicator.onSuccessPassCode = {
            when (it) {
                ReasonEnterCode.SEND_TRANSACTION -> {
                    setEvent(SwapSendEvent.PinConfirmed)
                }

                else -> Unit
            }
        }
    }

    fun queryWalletWithTokensList(type: String, fingerPrint: Long?) =
        walletInteract.getWalletWithTokensByFingerPrintNetworkTypeFlow(fingerPrint, type)

    suspend fun push_transaction(
        spendBundle: String,
        url: String,
        amount: Double,
        networkType: String,
        fingerPrint: Long,
        code: String,
        dest_puzzle_hash: String,
        address: String,
        fee: Double,
        spentCoinsJson: String,
        spentCoinsToken: String
    ) = blockChainInteract.push_tx(
        spendBundle,
        url,
        amount,
        networkType,
        fingerPrint,
        code,
        dest_puzzle_hash,
        address,
        fee,
        spentCoinsJson,
        spentCoinsToken
    )

    suspend fun checkIfAddressExistInDb(address: String) =
        addressInteract.checkIfAddressAlreadyExist(address = address)

    suspend fun getCoinDetailsFeeCommission(code: String) =
        greenAppInteract.getCoinDetails(code).fee_commission


    suspend fun getTokenPriceByCode(code: String) = tokenInteract.getTokenPriceByCode(code)

    suspend fun insertAddressEntity(address: Address) = addressInteract.insertAddressEntity(address)

    fun getSpentCoinsAmountsAddressCodeForSpendableBalance(
        address: String,
        tokenCode: String,
        networkType: String
    ) =
        spentCoinsInteract.getSumSpentCoinsForSpendableBalance(networkType, address, tokenCode)


    suspend fun swipedRefreshLayout(onFinished: () -> Unit) {
        blockChainInteract.updateBalanceAndTransactionsPeriodically()
        greenAppInteract.requestOtherNotifItems()
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
        }
    }

    fun updateFee(amount: Double) {
        _viewState.update { it.copy(fee = amount) }
        validateFeeEnough()
    }

    fun updateSendAmount(amount: Double) {
        _viewState.update { it.copy(sendAmount = amount) }
        validateFeeEnough()
    }

    fun updateSpendableBalance(wallet: WalletWithTokens) {
        viewModelScope.launch {
            spentCoinsInteract.getSpentCoinsBalanceByAddressAndCode(wallet.address, "XCH")
                .collect { amount ->
                    VLog.d("Wallet's With Amount : ${wallet.tokenWalletList[0].amount} on update spendable balance")
                    _viewState.update { it.copy(spendableBalance = wallet.tokenWalletList[0].amount - amount) }
                    validateFeeEnough()
                }
        }
    }


    private fun validateFeeEnough() {
        _viewState.update { it.copy(isFeeEnough = it.fee + it.sendAmount <= it.spendableBalance) }
    }

}
