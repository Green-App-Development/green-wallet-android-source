package com.green.wallet.data.local

import androidx.room.Dao
import androidx.room.Index.Order
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.green.wallet.data.local.entity.OrderEntity
import com.green.wallet.presentation.tools.OrderStatus
import kotlinx.coroutines.flow.Flow
import java.util.Optional


@Dao
interface OrderExchangeDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertOrderExchange(orderEntity: OrderEntity)

    @Query("SELECT * FROM OrderEntity WHERE order_hash=:order_hash")
    fun getOrderExchangeByOrderHash(order_hash: String): Flow<OrderEntity>

    @Query("SELECT * FROM OrderEntity WHERE status=:st1 OR status=:st2")
    suspend fun getOrderExchangeInProgressOrAwaitingPayment(
        st1: OrderStatus = OrderStatus.InProgress,
        st2: OrderStatus = OrderStatus.Waiting
    ): List<OrderEntity>

    @Query("UPDATE OrderEntity SET status=:status WHERE order_hash=:hash")
    suspend fun updateOrderStatusByHash(status: OrderStatus, hash: String)

    @Query("UPDATE OrderEntity SET tx_ID=:txID WHERE order_hash=:hash")
    suspend fun updateOrderTxIDByHash(txID: String, hash: String)

    @Query("SELECT * FROM OrderEntity ORDER BY time_created DESC")
    fun getAllOrderEntityList(): Flow<List<OrderEntity>>

    @Query("UPDATE OrderEntity SET expired_cancelled_time=:cancelledTime WHERE order_hash=:hash")
    suspend fun updateOrderEntitySetCancelledTimeByHash(hash: String, cancelledTime: Long): Int

}
