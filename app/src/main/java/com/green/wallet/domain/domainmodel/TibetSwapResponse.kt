package com.green.wallet.domain.domainmodel

data class TibetSwapResponse(
	val amount_in: Long,
	val amount_out: Long,
	val asset_id: String,
	val fee: Long?,
	val input_reserve: Long,
	val output_reserve: Long,
	val price_impact: Double,
	val price_warning: Boolean
)
