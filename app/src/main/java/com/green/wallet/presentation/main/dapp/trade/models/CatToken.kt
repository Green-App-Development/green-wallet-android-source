package com.green.wallet.presentation.main.dapp.trade.models

data class CatToken(
    val code: String,
    val assetID: String,
    override val amount: Double,
    val spendableBalance: Double = 0.0,
) : Token(amount)
