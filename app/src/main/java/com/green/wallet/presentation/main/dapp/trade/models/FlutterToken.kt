package com.green.wallet.presentation.main.dapp.trade.models

data class FlutterToken(
    val assetID: String,
    val amount: Long,
    val type: String,
    val fromAddress: String=""
)


