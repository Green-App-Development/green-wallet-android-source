package com.green.wallet.presentation.main.transaction.btmSpeedy

sealed interface SpeedyTokenEvent {

    object OnSign : SpeedyTokenEvent

    data class OnFeeChosen(val fee: Double) : SpeedyTokenEvent

    object OnSpeedError : SpeedyTokenEvent

    object OnSpeedSuccess : SpeedyTokenEvent

}