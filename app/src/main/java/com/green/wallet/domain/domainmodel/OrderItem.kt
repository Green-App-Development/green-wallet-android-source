package com.green.wallet.domain.domainmodel

import com.green.wallet.presentation.tools.OrderStatus

data class OrderItem(
	val hash: String,
	val amountToSend: Double,
	val giveAddress: String,
	val timeCreated: Long,
	val status: OrderStatus,
	val rate: Double,
	val getCoin: String,
	val sendCoin: String,
	val getAddress:String,
	val txID:String,
	val fee:Double
)
