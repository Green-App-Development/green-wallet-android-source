package com.green.wallet.data.network.dto.spendbundle

data class CoinSpend(
	val coin: CoinDto,
	val puzzle_reveal: String,
	val solution: String
)
