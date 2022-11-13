package com.android.greenapp.domain.domainmodel

data class SpentCoin(
    val parent_coin_info: String,
    val puzzle_hash: String,
    val amount: Long
)