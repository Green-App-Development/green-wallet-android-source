package com.green.wallet.presentation.main.swap.send

sealed interface SwapSendEvent {

    object PinConfirmed : SwapSendEvent

}