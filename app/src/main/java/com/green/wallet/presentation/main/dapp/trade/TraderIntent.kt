package com.green.wallet.presentation.main.dapp.trade

sealed interface TraderIntent {

    object ShowPinCode : TraderIntent

    data class ParseTakeOffer(val offer: String) : TraderIntent

    data class TakeOffer(val offer: String) : TraderEvent


}