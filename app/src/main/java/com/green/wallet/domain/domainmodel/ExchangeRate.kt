package com.green.wallet.domain.domainmodel

data class ExchangeRate(
	val min: Double,
	val max: Double,
	val give_address: String,
	val rate: Double,
	val commissionInPercent: String,
	val commissionXCH: String,
	val commissionTron:String
)
