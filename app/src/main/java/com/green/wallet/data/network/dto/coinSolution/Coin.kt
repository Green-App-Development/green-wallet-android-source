package com.green.wallet.data.network.dto.coinSolution

data class Coin(
    val amount: Int,
    val parent_coin_info: String,
    val puzzle_hash: String
)
