package com.green.wallet.presentation.main.dapp.trade

import com.green.wallet.presentation.main.dapp.trade.params.CreateOfferParams

sealed interface TraderEvent {

    object ShowPinCode : TraderEvent

    object ShowConnectionDialog : TraderEvent

    object ShowTakeOfferDialog : TraderEvent

    data class ParseTakeOffer(val offer: String) : TraderEvent

    data class TakeOffer(val offer: String) : TraderEvent

    data class ShowCreateOfferDialog(val params: CreateOfferParams) : TraderEvent

    object SuccessTakingOffer : TraderEvent

    object SendTakeOfferResult : TraderEvent

    data class SendCreateOfferResult(val offer: String) : TraderEvent

    object FailureTakingOffer : TraderEvent

    data class ChoseFee(val fee: Double) : TraderEvent

    object PinConfirmAcceptOffer : TraderEvent

    object PinnedCreateOffer : TraderEvent
    object ShowPinCreateOffer : TraderEvent

}