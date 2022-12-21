package com.green.wallet.data.network.dto.coins

data class CoinRecordResponse(
	val coin_records: List<CoinRecord>,
	val success: Boolean
)
