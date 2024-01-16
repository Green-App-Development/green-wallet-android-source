package com.green.wallet.presentation.main.send

data class TransferState(
    val isLoading: Boolean = false,
    val dexieFee: Double = 0.0,
    val xchSpendableBalance: Double = 0.0,
    val chosenFee: Double = 0.0,
    val xchSendingAmount: Double = 0.0,
    val catSendingAmount: Double = 0.0,
    val catSpendableAmount: Double = 0.0
)