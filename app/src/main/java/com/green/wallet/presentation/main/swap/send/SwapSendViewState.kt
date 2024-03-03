package com.green.wallet.presentation.main.swap.send

data class SwapSendViewState(
    val fee: Double = 0.0,
    val spendableBalance: Double = 0.0,
    val dexieFee: Double = 0.0,
    val isFeeEnough: Boolean = false,
    val sendAmount:Double = 0.0
)