package com.green.wallet.domain.domainmodel

import android.os.Parcelable
import com.green.wallet.data.local.entity.TibetSwapEntity
import com.green.wallet.presentation.tools.OrderStatus
import kotlinx.android.parcel.Parcelize


@Parcelize
data class TibetSwapExchange(
	val offer_id: String,
	val send_amount: Double,
	val receive_amount: Double,
	val send_coin: String,
	val receive_coin: String,
	val fee: Double,
	val time_created: Long,
	val status: OrderStatus,
	val height: Int,
	val fk_address: String
) : Parcelable {

	fun toTibetSwapEntity() = TibetSwapEntity(
		offer_id,
		send_amount,
		receive_amount,
		send_coin,
		receive_coin,
		fee,
		time_created,
		fk_address,
		status,
		height
	)


}
