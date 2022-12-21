package com.green.wallet.data.network.dto.coins

data class Coin(
    val amount: Long,
    val parent_coin_info: String,
    val puzzle_hash: String
)
