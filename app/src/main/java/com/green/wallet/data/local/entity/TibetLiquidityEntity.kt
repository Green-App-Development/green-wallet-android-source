package com.green.wallet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.green.wallet.presentation.tools.OrderStatus


@Entity
data class TibetLiquidityEntity(
	@PrimaryKey(autoGenerate = false)
	val offer_id: String,
	val xchAmount: Double,
	val catAmount: Double,
	val catToken: String,
	val liquidityAmount: Double,
	val liquidityToken: Double,
	val fee: Double,
	val time_created: Long,
	val status: OrderStatus = OrderStatus.InProgress,
	val height: Int = 0,
	val addLiquidity: Boolean
){

}
