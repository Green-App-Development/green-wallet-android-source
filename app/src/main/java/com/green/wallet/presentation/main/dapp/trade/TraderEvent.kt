package com.green.wallet.presentation.main.dapp.trade

import com.green.wallet.presentation.main.dapp.trade.params.CreateOfferParams

sealed interface TraderEvent {

    object ShowPinCode : TraderEvent

    object ShowConnectionDialog : TraderEvent

    object ShowTakeOfferDialog : TraderEvent

    data class ParseTakeOffer(val offer: String) : TraderEvent

    data class SignOffer(val offer: String) : TraderEvent

    data class ShowCreateOfferDialog(val params: CreateOfferParams) : TraderEvent

    object SuccessTakingOffer : TraderEvent

    object FailureTakingOffer : TraderEvent

    data class ChoseFee(val fee: Double) : TraderEvent

    object PinConfirmAcceptOffer : TraderEvent

    data class CreateOfferFlutter(val params: CreateOfferParams, val fee: Double) : TraderEvent

}