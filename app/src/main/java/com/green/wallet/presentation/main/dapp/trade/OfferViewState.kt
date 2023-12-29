package com.green.wallet.presentation.main.dapp.trade

import com.green.wallet.presentation.main.dapp.trade.models.TokenOffer

data class OfferViewState(
    val acceptOffer: Boolean = false,
    val offered: List<TokenOffer> = emptyList(),
    val requested: List<TokenOffer> = emptyList(),
    val offer: String = "",
)