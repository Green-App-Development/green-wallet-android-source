package com.green.wallet.presentation.main.swap.tibetswap

data class TibetSwapState(
    val dexieFee: Double = 0.0,
    val feeEnough: Boolean = false,
    val fee:Double = 0.0,
    val spendableBalance: Double = 0.0
)