package com.green.wallet.domain.domainmodel

data class ExchangeRate(
	val min: Double,
	val max: Double,
	val give_address: String,
	val rateXCH: Double,
	val rateUSDT: Double,
	val commissionInPercent: String,
	val commission: String
)
