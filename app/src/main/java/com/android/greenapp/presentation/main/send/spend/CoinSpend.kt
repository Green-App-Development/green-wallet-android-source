package com.android.greenapp.presentation.main.send.spend

data class CoinSpend(
    val coin: Coin,
    val puzzle_reveal: String,
    val solution: String
)