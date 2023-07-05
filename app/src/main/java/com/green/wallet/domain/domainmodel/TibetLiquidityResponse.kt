package com.green.wallet.domain.domainmodel

class TibetLiquidityResponse(
	val asset_id: String,
	val last_coin_id_on_chain: String,
	val launcher_id: String,
	val liquidity: Long,
	val liquidity_asset_id: String,
	val token_reserve: Long,
	val xch_reserve: Long
)
