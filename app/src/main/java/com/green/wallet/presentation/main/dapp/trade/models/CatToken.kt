package com.green.wallet.presentation.main.dapp.trade.models

data class CatToken(
    val code: String = "",
    val assetID: String = "",
    var amount: Double = 0.0,
    val spendableBalance: Double = 0.0,
) : Token()
