package com.green.wallet.presentation.main.send

data class TransferState(
    val isLoading: Boolean = false,
    val dexieFee: Double = 0.0,
    val spendableBalance:Double = 0.0
)