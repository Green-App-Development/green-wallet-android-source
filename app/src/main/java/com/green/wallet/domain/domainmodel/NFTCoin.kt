package com.green.wallet.domain.domainmodel

data class NFTCoin(
    val coinInfo: String,
    val addressFk: String,
    val coinHash: String,
    val amount: Int,
    val confirmedBlockIndex: Long,
    val spentBlockIndex: Long,
    val timeStamp: Long,
    val puzzleHash:String
)
