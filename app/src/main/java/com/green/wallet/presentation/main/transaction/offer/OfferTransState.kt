package com.green.wallet.presentation.main.transaction.offer

import com.green.wallet.presentation.main.dapp.trade.models.Token

data class OfferTransState(
    val time: Long = 0L,
    val requested: List<Token> = emptyList(),
    val offered: List<Token> = emptyList(),
    val source: String = "",
    val hashTransaction: String = " ",
    val fee: Double = 0.0,
    val height: Long = 0L,
    val acceptOffer: Boolean = true
)