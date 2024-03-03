package com.green.wallet.presentation.main.transaction.btmCancel

import androidx.lifecycle.viewModelScope
import com.green.wallet.domain.domainmodel.OfferTransaction
import com.green.wallet.domain.domainmodel.Wallet
import com.green.wallet.domain.interact.DexieInteract
import com.green.wallet.domain.interact.OfferTransactionInteract
import com.green.wallet.domain.interact.SpentCoinsInteract
import com.green.wallet.domain.interact.WalletInteract
import com.green.wallet.presentation.tools.VLog
import com.greenwallet.core.base.BaseViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class CancelOfferViewModel @Inject constructor(
    private val offerTransactionInteract: OfferTransactionInteract,
    private val dexieInteract: DexieInteract,
    private val walletInteract: WalletInteract,
    private val coinsInteract: SpentCoinsInteract
) : BaseViewModel<CancelOfferState, CancelOfferEvent>(CancelOfferState()) {

    private lateinit var offerTransaction: OfferTransaction
    private lateinit var wallet: Wallet

    fun initOfferTransaction(tranID: String) {
        viewModelScope.launch {
//            offerTransaction = offerTransactionInteract.getOfferTransactionByTranID(tranID)
            _viewState.update { it.copy(addressFk = "xch1ja9cl4hum8v85m8wn0yt5sphf8jxn65ex8f77frk6wrqx2a2hzts60j58s") }

            initWallet("xch1ja9cl4hum8v85m8wn0yt5sphf8jxn65ex8f77frk6wrqx2a2hzts60j58s")
            getSpentCoinsSum()
        }

        viewModelScope.launch {
            val dexieRecommendedFee = dexieInteract.getDexieMinFee().recommended
            _viewState.update {
                it.copy(normalFeeDexie = dexieRecommendedFee, fee = dexieRecommendedFee)
            }
        }
    }

    private suspend fun initWallet(addressFK: String) {
        wallet = walletInteract.getWalletByAddress(addressFK)
    }

    fun handleEvent(event: CancelOfferEvent) {
        when (event) {
            is CancelOfferEvent.OnFeeChosen -> {
                _viewState.update { it.copy(fee = event.amount) }
            }

            else -> Unit
        }
    }

    private fun getSpentCoinsSum() {
        viewModelScope.launch {
            coinsInteract.getSpentCoinsBalanceByAddressAndCode(wallet.address, "XCH")
                .collectLatest { it ->
                    val feeSpendable = wallet.balance - it
                    VLog.d("Wallet Balance : ${wallet.balance}, Spent Coins Fee : $it")
                    _viewState.update {
                        it.copy(
                            spendableBalance = Math.max(feeSpendable, 0.0)
                        )
                    }
                }
        }
    }

}