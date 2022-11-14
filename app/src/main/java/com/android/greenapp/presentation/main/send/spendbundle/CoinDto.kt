package com.android.greenapp.presentation.main.send.spend

data class CoinDto(
    var amount: Long,
    var parent_coin_info: String,
    var puzzle_hash: String
)
