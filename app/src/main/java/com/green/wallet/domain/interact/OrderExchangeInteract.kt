package com.green.wallet.domain.interact

import com.green.wallet.domain.domainmodel.OrderItem

interface OrderExchangeInteract {


	suspend fun insertOrderItem(orderItem: OrderItem)

}
