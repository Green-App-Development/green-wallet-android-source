package com.green.wallet.presentation.main.transaction.btmSpeedy

import com.green.wallet.presentation.main.dapp.trade.models.Token

data class SpeedyTokenState(
    val isLoading: Boolean = false,
    val token: Token? = null,
    val fee: Double = 0.0,
    val spendableFee: Double = 0.0
)