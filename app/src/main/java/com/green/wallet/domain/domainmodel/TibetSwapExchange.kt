package com.green.wallet.domain.domainmodel

import com.green.wallet.data.local.entity.TibetSwapEntity

data class TibetSwapExchange(
	val offer_id: String,
	val send_amount: Double,
	val receive_amount: Double,
	val send_coin: String,
	val receive_coin: String,
	val fee: Double,
	val time_created: Long
) {
	fun toTibetSwapEntity() = TibetSwapEntity(
		offer_id,
		send_amount,
		receive_amount,
		send_coin,
		receive_coin,
		fee,
		time_created
	)
}
