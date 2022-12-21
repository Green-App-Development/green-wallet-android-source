package com.green.wallet.data.network.dto.coins

data class CoinRecord(
    val coin: Coin,
    val coinbase: Boolean,
    val confirmed_block_index: Long,
    val spent: Boolean,
    val spent_block_index: Int,
    val timestamp: Long
)
