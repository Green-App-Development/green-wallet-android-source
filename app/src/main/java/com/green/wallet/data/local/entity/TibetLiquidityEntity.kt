package com.green.wallet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.green.wallet.domain.domainmodel.TibetLiquidity
import com.green.wallet.presentation.tools.OrderStatus


@Entity
data class TibetLiquidityEntity(
	@PrimaryKey(autoGenerate = false)
	val offer_id: String,
	val xchAmount: Double,
	val catAmount: Double,
	val catToken: String,
	val liquidityAmount: Double,
	val liquidityToken: String,
	val fee: Double,
	val time_created: Long,
	val status: OrderStatus = OrderStatus.InProgress,
	val height: Int = 0,
	val addLiquidity: Boolean,
	val fk_address: String
) {

	fun toTibetLiquidity() = TibetLiquidity(
		offer_id,
		xchAmount,
		catAmount,
		catToken,
		liquidityAmount,
		liquidityToken,
		fee,
		time_created,
		status,
		height,
		addLiquidity,
		fk_address
	)

}
