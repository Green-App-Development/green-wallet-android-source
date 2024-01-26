package com.green.wallet.presentation.main.dapp.trade

import com.green.wallet.presentation.main.dapp.trade.models.Token

data class OfferViewState(
    val acceptOffer: Boolean = false,
    val offered: List<Token> = emptyList(),
    val requested: List<Token> = emptyList(),
    val offer: String = "",
    val chosenFee: Double = 0.0,
    val feeEnough: Boolean = false,
    val spendableBalance: Double = 0.0,
    val dexieFee: Double = 0.0,
)