package com.green.wallet.data.network.dto.tibet

import com.green.wallet.domain.domainmodel.TibetSwapResponse

data class TibetSwapDto(
	val amount_in: Long,
	val amount_out: Long,
	val asset_id: String,
	val fee: Long?,
	val input_reserve: Long,
	val output_reserve: Long,
	val price_impact: Double,
	val price_warning: Boolean
) {

	fun toTibetSwap() = TibetSwapResponse(
		amount_in,
		amount_out,
		asset_id,
		fee,
		input_reserve,
		output_reserve,
		price_impact,
		price_warning
	)


}
