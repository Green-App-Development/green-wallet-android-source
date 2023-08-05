package com.green.wallet.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.green.wallet.domain.domainmodel.OrderItem
import com.green.wallet.presentation.tools.OrderStatus
import com.green.wallet.presentation.tools.OrderType


@Entity(tableName = "OrderEntity")
data class OrderEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "order_hash")
    val order_hash: String,
    val status: OrderStatus,
    val amount_to_send: Double,
    val give_address: String,
    val time_created: Long,
    val rate: Double,
    val send_coin: String,
    val get_coin: String,
    val get_address: String,
    val tx_ID: String,
    val fee: Double,
    val amount_to_receive: Double
) {

    fun toOrderItem() =
        OrderItem(
            order_hash,
            amount_to_send,
            give_address,
            time_created,
            status,
            rate,
            get_coin,
            send_coin,
            get_address,
            tx_ID,
            fee = fee,
            amount_to_receive
        )


}
