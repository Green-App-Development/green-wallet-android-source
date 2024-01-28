package com.green.wallet.presentation.main.swap.tibetswap

sealed interface TibetSwapEvent {

    object SuccessSwap : TibetSwapEvent
    object ErrorSwap : TibetSwapEvent

}