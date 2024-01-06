package com.green.wallet.presentation.main.dapp.trade

import com.green.wallet.presentation.main.dapp.trade.models.Token

data class OfferViewState(
    val acceptOffer: Boolean = false,
    val offered: List<Token> = emptyList(),
    val requested: List<Token> = emptyList(),
    val offer: String = "",
)