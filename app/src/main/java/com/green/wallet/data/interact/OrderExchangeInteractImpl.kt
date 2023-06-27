package com.green.wallet.data.interact

import com.green.wallet.data.local.OrderExchangeDao
import com.green.wallet.domain.domainmodel.OrderItem
import com.green.wallet.domain.interact.OrderExchangeInteract
import javax.inject.Inject

class OrderExchangeInteractImpl @Inject constructor(
	private val orderExchangeDao: OrderExchangeDao
) : OrderExchangeInteract {


	override suspend fun insertOrderItem(orderItem: OrderItem) {
		val orderEntity = orderItem.toOrderEntity()
		orderExchangeDao.insertOrderExchange(orderEntity)
	}


}
