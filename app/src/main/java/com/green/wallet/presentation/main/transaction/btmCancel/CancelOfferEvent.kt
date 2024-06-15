package com.green.wallet.presentation.main.transaction.btmCancel

sealed interface CancelOfferEvent {

    object OnSign : CancelOfferEvent

    object ShowPinCode : CancelOfferEvent

    object PinnedCodeToCancel : CancelOfferEvent

    object ErrorCancelled : CancelOfferEvent

    object SuccessSending : CancelOfferEvent

    data class OnFeeChosen(val amount: Double) : CancelOfferEvent

}