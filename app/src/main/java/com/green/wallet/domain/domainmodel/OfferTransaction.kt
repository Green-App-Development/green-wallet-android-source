package com.green.wallet.domain.domainmodel

import com.green.wallet.presentation.main.dapp.trade.models.CatToken
import com.green.wallet.presentation.main.dapp.trade.models.Token


data class OfferTransaction(
    val transId: String = "",
    val time: Long = 0L,
    val requested: List<Token> = listOf(
        CatToken("XCH", "", 0.00003),
        CatToken("XCH", "", 0.00003),
    ),
    val offered: List<Token> = listOf(
        CatToken("XCH", "", 0.00003),
    ),
    val source: String = "",
    val hashTransaction: String = " ",
    val fee: Double = 0.0,
    val height: Long = 0L,
    val acceptOffer: Boolean = true,
) : Transaction