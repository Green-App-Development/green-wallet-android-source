package com.green.wallet.presentation.main.dapp.trade

import com.green.wallet.presentation.main.dapp.trade.models.OfferToken

data class OfferViewState(
    val acceptOffer: Boolean = false,
    val offered: List<OfferToken> = emptyList(),
    val requested: List<OfferToken> = emptyList(),
    val offer: String = "",
)