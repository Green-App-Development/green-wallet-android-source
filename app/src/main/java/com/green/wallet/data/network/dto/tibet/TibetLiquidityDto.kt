package com.green.wallet.data.network.dto.tibet

import com.green.wallet.domain.domainmodel.TibetLiquidity

data class TibetLiquidityDto(
	val asset_id: String,
	val last_coin_id_on_chain: String,
	val launcher_id: String,
	val liquidity: Long,
	val liquidity_asset_id: String,
	val token_reserve: Long,
	val xch_reserve: Long
) {

	fun toTibetLiquidity() = TibetLiquidity(
		asset_id,
		last_coin_id_on_chain,
		launcher_id,
		liquidity,
		liquidity_asset_id,
		token_reserve,
		xch_reserve
	)


}
