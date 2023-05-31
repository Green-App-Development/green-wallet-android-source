package com.green.wallet.domain.domainmodel

import com.green.wallet.presentation.tools.OrderStatus

data class OrderItem(
	val hash: String,
	val amountToSend: Double,
	val give_address: String,
	val code: String,
	val time_created: Long,
	val status: OrderStatus
)
