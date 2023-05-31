package com.green.wallet.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.green.wallet.domain.domainmodel.OrderItem
import com.green.wallet.presentation.tools.OrderStatus


@Entity(tableName = "OrderEntity")
data class OrderEntity(
	@PrimaryKey(autoGenerate = false)
	@ColumnInfo(name = "order_hash")
	val order_hash: String,
	val status: OrderStatus,
	val amount_to_send: Double,
	val give_address: String,
	val code: String,
	val time_created: Long
) {

	fun toOrderItem() =
		OrderItem(order_hash, amount_to_send, give_address, code, time_created, status)



}
