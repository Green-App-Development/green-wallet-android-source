package com.green.wallet.presentation.main.dapp.trade.models

data class CatToken(
    val code: String,
    val assetID: String,
    override val amount: Double
) : TokenOffer(amount)
