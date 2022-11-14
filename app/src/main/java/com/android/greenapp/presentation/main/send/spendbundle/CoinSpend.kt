package com.android.greenapp.presentation.main.send.spend

data class CoinSpend(
	val coin: CoinDto,
	val puzzle_reveal: String,
	val solution: String
)
