package com.green.wallet.presentation.main.transaction.btmCancel

sealed interface CancelOfferEvent {

    object OnSign : CancelOfferEvent

    data class OnFeeChosen(val amount: Double) : CancelOfferEvent

}