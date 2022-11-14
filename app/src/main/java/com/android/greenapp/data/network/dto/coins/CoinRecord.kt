package com.android.greenapp.data.network.dto.coins

data class CoinRecord(
    val coin: Coin,
    val coinbase: Boolean,
    val confirmed_block_index: Int,
    val spent: Boolean,
    val spent_block_index: Int,
    val timestamp: Long
)