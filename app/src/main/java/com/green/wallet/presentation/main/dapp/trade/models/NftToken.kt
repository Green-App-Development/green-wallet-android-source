package com.green.wallet.presentation.main.dapp.trade.models

data class NftToken(
    var collection: String,
    var nftId: String,
    val imgUrl: String,
    val nftCoinHash:String=""
) : Token(1.0)