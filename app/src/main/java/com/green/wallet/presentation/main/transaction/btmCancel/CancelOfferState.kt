package com.green.wallet.presentation.main.transaction.btmCancel

data class CancelOfferState(
    val isLoading: Boolean = false,
    val spendableBalance: Double = 0.0,
    val fee: Double = 0.0,
    val normalFeeDexie: Double = 0.0,
    val addressFk: String = ""
)