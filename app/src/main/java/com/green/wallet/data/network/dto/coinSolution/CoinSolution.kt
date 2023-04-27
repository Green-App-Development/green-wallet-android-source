package com.green.wallet.data.network.dto.coinSolution

data class CoinSolution(
    val coin: Coin,
    val puzzle_reveal: String,
    val solution: String
)
