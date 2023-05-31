package com.green.wallet.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.green.wallet.data.local.entity.OrderEntity


@Dao
interface OrderExchangeDao {


	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertOrderExchange(orderEntity: OrderEntity)



}
