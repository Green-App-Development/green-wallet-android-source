package com.green.wallet.data.network.dto.spendbundle

data class CoinDto(
    var amount: Long,
    var parent_coin_info: String,
    var puzzle_hash: String
)
