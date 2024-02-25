package com.green.wallet.presentation.main.dapp.trade.models

import com.fasterxml.jackson.annotation.JsonIgnore

data class NftToken(
    var collection: String = "",
    var nftId: String = "",
    @JsonIgnore
    val imgUrl: String = "",
    val nftCoinHash: String = ""
) : Token()