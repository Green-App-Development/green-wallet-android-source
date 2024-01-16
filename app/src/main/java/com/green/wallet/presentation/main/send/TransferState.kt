package com.green.wallet.presentation.main.send

data class TransferState(
    val isLoading: Boolean = false,
    val dexieFee: Double = 0.0,
    val xchSpendableBalance: Double = 0.0,
    val chosenFee: Double = 0.0,
    val sendingAmount: Double = 0.0,
    val catSpendableAmount: Double = 0.0,
    val isSendingAmountEnough: Boolean = false,
    val isFeeEnough: Boolean = false,
    val xchSending: Boolean = false,
    val amountValid: Boolean = false
)