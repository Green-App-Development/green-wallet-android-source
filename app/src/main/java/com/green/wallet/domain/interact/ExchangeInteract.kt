package com.green.wallet.domain.interact

import com.green.wallet.domain.domainmodel.ExchangeRate
import com.green.wallet.domain.domainmodel.OrderItem
import com.green.wallet.domain.domainmodel.TibetSwapExchange
import com.green.wallet.presentation.tools.Resource
import kotlinx.coroutines.flow.Flow

interface ExchangeInteract {

	suspend fun createExchangeRequest(
		give_address: String,
		give_amount: Double,
		get_address: String,
		get_coin: String,
		rate: Double,
	): Resource<String>

	suspend fun getExchangeRequest(fromToken: String): Resource<ExchangeRate>

	suspend fun getOrderByHash(hash: String): OrderItem

	suspend fun updateOrderStatusPeriodically()

	suspend fun updateOrderStatusByHash(hash: String)

	fun getAllOrderEntityList(): Flow<List<OrderItem>>

	suspend fun insertTibetSwap(tibetSwapExchange: TibetSwapExchange)


}
