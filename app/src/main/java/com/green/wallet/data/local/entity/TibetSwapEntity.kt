package com.green.wallet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.green.wallet.domain.domainmodel.TibetSwapExchange


@Entity(tableName = "TibetSwapEntity")
data class TibetSwapEntity(
	@PrimaryKey(autoGenerate = false)
	val offer_id: String,
	val send_amount: Double,
	val receive_amount: Double,
	val send_coin: String,
	val receive_coin: String,
	val fee: Double,
	val time_created: Long
) {

	fun toTibetSwapExchange() = TibetSwapExchange(
		offer_id,
		send_amount,
		receive_amount,
		send_coin,
		receive_coin,
		fee,
		time_created
	)

}
