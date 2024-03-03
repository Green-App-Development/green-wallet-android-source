package com.green.wallet.presentation.main.transaction.btmCancel

import androidx.lifecycle.viewModelScope
import com.green.wallet.domain.domainmodel.OfferTransaction
import com.green.wallet.domain.interact.DexieInteract
import com.green.wallet.domain.interact.OfferTransactionInteract
import com.green.wallet.presentation.tools.VLog
import com.greenwallet.core.base.BaseViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class CancelOfferViewModel @Inject constructor(
    private val offerTransactionInteract: OfferTransactionInteract,
    private val dexieInteract: DexieInteract
) : BaseViewModel<CancelOfferState, CancelOfferEvent>(CancelOfferState()) {

    private lateinit var offerTransaction: OfferTransaction

    fun initOfferTransaction(tranID: String) {
        viewModelScope.launch {
            VLog.d("Found offer transaction by id : $tranID")
//            offerTransaction = offerTransactionInteract.getOfferTransactionByTranID(tranID)
            _viewState.update { it.copy(addressFk = "afdlasdkalsjdlsadlas") }
        }
        viewModelScope.launch {
            val dexieRecommendedFee = dexieInteract.getDexieMinFee().recommended
            _viewState.update {
                it.copy(normalFeeDexie = dexieRecommendedFee, fee = dexieRecommendedFee)
            }
        }
    }

    fun handleEvent(event: CancelOfferEvent) {
        when (event) {
            is CancelOfferEvent.OnFeeChosen -> {
                _viewState.update { it.copy(fee = event.amount) }
            }

            is CancelOfferEvent.OnSign -> {

            }
        }
    }

}