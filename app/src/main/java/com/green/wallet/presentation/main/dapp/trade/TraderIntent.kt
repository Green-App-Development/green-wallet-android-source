package com.green.wallet.presentation.main.dapp.trade

sealed interface TraderIntent {

    object CloseBtmOffer : TraderIntent

}