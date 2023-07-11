package com.green.wallet.data.interact

import com.example.common.tools.mapNetworkOrderStatusToLocal
import com.green.wallet.data.local.Converters
import com.green.wallet.data.local.OrderExchangeDao
import com.green.wallet.data.local.SpentCoinsDao
import com.green.wallet.data.local.TibetDao
import com.green.wallet.data.local.entity.OrderEntity
import com.green.wallet.data.local.entity.TibetSwapEntity
import com.green.wallet.data.network.DexieService
import com.green.wallet.data.network.ExchangeService
import com.green.wallet.data.network.dto.exchangestatus.ExchangeStatus
import com.green.wallet.data.preference.PrefsManager
import com.green.wallet.domain.domainmodel.ExchangeRate
import com.green.wallet.domain.domainmodel.OrderItem
import com.green.wallet.domain.domainmodel.TibetLiquidity
import com.green.wallet.domain.domainmodel.TibetSwapExchange
import com.green.wallet.domain.interact.ExchangeInteract
import com.green.wallet.domain.interact.PrefsInteract
import com.green.wallet.presentation.custom.NotificationHelper
import com.green.wallet.presentation.custom.parseException
import com.green.wallet.presentation.tools.OrderStatus
import com.green.wallet.presentation.tools.Resource
import com.green.wallet.presentation.tools.VLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ExchangeInteractImpl @Inject constructor(
	private val exchangeService: ExchangeService,
	private val prefsInteract: PrefsInteract,
	private val orderExchangeDao: OrderExchangeDao,
	private val notifHelper: NotificationHelper,
	private val tibetDao: TibetDao,
	private val dexieService: DexieService,
	private val spentCoinsDao: SpentCoinsDao
) : ExchangeInteract {


	override suspend fun createExchangeRequest(
		give_address: String,
		give_amount: Double,
		get_address: String,
		get_coin: String,
		rate: Double
	): Resource<String> {
		try {
			val guid = prefsInteract.getSettingString(PrefsManager.USER_GUID, "")
			val mapFields = hashMapOf<String, Any>()
			mapFields["user"] = guid
			mapFields["give_address"] = give_address
			mapFields["give_amount"] = give_amount
			mapFields["get_address"] = get_address
			mapFields["get_coin"] = get_coin
			val res = exchangeService.createExchangeRequest(
				mapFields
			)
			if (res.isSuccessful) {
				val body = res.body()!!
				val success = body.asJsonObject.get("success").asBoolean
				if (success) {
					val orderHash = body.asJsonObject.get("result").asString
					val orderExchange = OrderEntity(
						order_hash = orderHash,
						status = OrderStatus.Waiting,
						amount_to_send = give_amount,
						give_address = give_address,
						time_created = System.currentTimeMillis(),
						rate = rate,
						get_coin = get_coin,
						send_coin = if (get_coin == "XCH") "USDT" else "XCH",
						get_address = get_address,
						tx_ID = "",
						fee = 0.0
					)
					orderExchangeDao.insertOrderExchange(orderExchange)
					return Resource.success(orderHash)
				}
				val error_code = body.asJsonObject.get("error_code").asInt
				parseException(error_code)
			} else
				throw Exception("Request is not successful : ${res.message()}")
		} catch (ex: Exception) {
			VLog.d("Exception in creating exchange requesting : ${ex.message}")
			return Resource.error(ex)
		}
		return Resource.error(Exception("Unknown exception"))
	}

	override suspend fun getExchangeRequest(fromToken: String): Resource<ExchangeRate> {
		try {
			val guid = prefsInteract.getSettingString(PrefsManager.USER_GUID, "")
			val request = exchangeService.getExchangeRequestRate(user = guid)
			if (request.isSuccessful) {
				val res = request.body()!!.result
				if (request.body()!!.success) {
					if (res.fromUSDT != null && res.fromXCH != null) {
						val resExchange = when (fromToken) {
							"USDT" -> {
								ExchangeRate(
									min = res.fromUSDT.toXCH.min.toDoubleOrNull() ?: 0.0,
									max = res.fromUSDT.toXCH.max.toDoubleOrNull() ?: 0.0,
									give_address = res.fromUSDT.address,
									rateXCH = res.fromUSDT.toXCH.rate.toDoubleOrNull() ?: 0.0,
									rateUSDT = res.fromXCH.toUSDT.rate.toDoubleOrNull() ?: 0.0,
									res.fromUSDT.toXCH.usdtFee.exchange,
									res.fromUSDT.toXCH.usdtFee.usdt,
								)
							}

							else -> {
								ExchangeRate(
									min = res.fromXCH.toUSDT.min.toDoubleOrNull() ?: 0.0,
									max = res.fromXCH.toUSDT.max.toDoubleOrNull() ?: 0.0,
									give_address = res.fromXCH.address,
									rateXCH = res.fromUSDT.toXCH.rate.toDoubleOrNull() ?: 0.0,
									rateUSDT = res.fromXCH.toUSDT.rate.toDoubleOrNull() ?: 0.0,
									res.fromXCH.toUSDT.xchFee.exchange,
									res.fromXCH.toUSDT.xchFee.xch,
								)
							}
						}
						return Resource.success(resExchange)
					}
					return Resource.success(ExchangeRate(0.0, 0.0, "", 0.0, 0.0, "", ""))
				}
			} else {
				VLog.d("Request is not success for exchange rate : ${request.message()}")
			}
		} catch (ex: Exception) {
			VLog.d("Exception caught in getting exchange request : ${ex.message}")
			return Resource.error(ex)
		}
		return Resource.error(Exception("Unknown error"))
	}

	override suspend fun getOrderByHash(hash: String): OrderItem {
		val res = orderExchangeDao.getOrderExchangeByOrderHash(order_hash = hash)
		return res.get().toOrderItem()
	}

	override suspend fun updateOrderStatusPeriodically() {
		val ordersList = orderExchangeDao.getOrderExchangeInProgressOrAwaitingPayment()
		for (order in ordersList) {
			val exchangeStatus = getOrderStatus(order.order_hash)
			if (exchangeStatus != null) {
				val status = mapNetworkOrderStatusToLocal(exchangeStatus.result.status)
				VLog.d("ExchangeStatus : $exchangeStatus of orderItem : $order")
				if (status != order.status) {
					orderExchangeDao.updateOrderStatusByHash(status, order.order_hash)
					val resLanguageResource =
						prefsInteract.getSettingString(PrefsManager.LANGUAGE_RESOURCE, "")
					val resMap = Converters.stringToHashMap(resLanguageResource)
					val messageOrderStatusUpdate =
						resMap["notif_status_updated"] ?: "New exchange request status"

					if (status == OrderStatus.Success) {
						orderExchangeDao.updateOrderTxIDByHash(
							exchangeStatus.result.get.txID ?: "",
							order.order_hash
						)
						val statusCompleted = resMap["status_completed"] ?: "Completed"
						val message =
							messageOrderStatusUpdate + " ${order.order_hash} $statusCompleted"
						notifHelper.callGreenAppNotificationMessages(
							message,
							System.currentTimeMillis()
						)
					} else if (status == OrderStatus.Cancelled) {
						val statusCancelled = resMap["status_canceled"] ?: "Cancelled"
						val message =
							messageOrderStatusUpdate + " ${order.order_hash} $statusCancelled"
						notifHelper.callGreenAppNotificationMessages(
							message,
							System.currentTimeMillis()
						)
					}
				}
			} else
				VLog.d("Exchange Status is null : $order")
		}
	}

	override suspend fun updateOrderStatusByHash(hash: String) {

	}

	override fun getAllOrderListFlow(): Flow<List<Any>> {
		val ordersFlow =
			orderExchangeDao.getAllOrderEntityList().map { it.map { it.toOrderItem() } }
		val tibetSwapFlow =
			tibetDao.getTibetSwapEntitiesListFlow().map {
				it.map {
					it.toTibetSwapExchange()
				}
			}

		val tibetLiquidityOrder =
			tibetDao.getTibetLiquidityEntityListFlow().map { it.map { it.toTibetLiquidity() } }

		return combine(
			ordersFlow,
			tibetSwapFlow,
			tibetLiquidityOrder
		) { orders, tibetSwap, liquidityList ->
			(orders + tibetSwap + liquidityList).sortedBy { item ->
				when (item) {
					is OrderEntity -> item.time_created
					is TibetSwapEntity -> item.time_created
					is TibetLiquidity -> item.time_created
					else -> 0L
				}
			}
		}

	}

	override suspend fun insertTibetSwap(tibetSwapExchange: TibetSwapExchange) {
		val tibet = tibetSwapExchange.toTibetSwapEntity()
		VLog.d("Inserting tibet swap exchange : $tibetSwapExchange")
		tibetDao.insertTibetEntity(tibet)
	}

	override suspend fun updateTibetSwapExchangeStatus() {
		val tibetSwapList = tibetDao.getTibetSwapListInProgressStatus(OrderStatus.InProgress)
		for (tibetSwap in tibetSwapList) {
			val spentHeight = getTibetSwapSpentHeight(tibetSwap)
			if (spentHeight != null) {
				notifHelper.callGreenAppNotificationMessages(
					"Tibet Swap XCH CAT : ${tibetSwap.offer_id} success ",
					System.currentTimeMillis()
				)
				tibetDao.updateTibetSwapEntityStatusToCompleted(
					status = OrderStatus.Success,
					offer_id = tibetSwap.offer_id
				)
				tibetDao.updateTibetSwapEntityHeightToCompleted(
					height = spentHeight,
					offer_id = tibetSwap.offer_id
				)
				val rows = spentCoinsDao.deleteSpentConsByTimeCreated(tibetSwap.time_created)
				VLog.d("Deleted spent coins row affected : $rows")
			}
		}
	}

	override fun getTibetSwapDetailByOfferId(offerId: String): Flow<TibetSwapExchange> {
		return tibetDao.getTibetSwapEntityByOfferId(offerId).map { it.toTibetSwapExchange() }
	}

	override suspend fun updateTibetLiquidityStatus() {
		val liquidList = tibetDao.getTibetLiquidityStatusInProgress()

	}

	private suspend fun getTibetSwapSpentHeight(tibetSwap: TibetSwapEntity): Int? {
		try {
			val res = dexieService.getTibetSwapOfferStatus(tibetSwap.offer_id)
			if (res.isSuccessful) {
				val spentIndex =
					res.body()!!.asJsonObject.get("offer").asJsonObject.get("spent_block_index")?.asInt
				if (spentIndex != null)
					return spentIndex
				return null
			} else
				throw Exception(res.message())
		} catch (ex: Exception) {
			VLog.d("Exception in getting tibet swap status : ${ex.message}")
		}
		return null
	}

	private suspend fun getOrderStatus(orderHash: String): ExchangeStatus? {
		try {
			val guid = prefsInteract.getSettingString(PrefsManager.USER_GUID, "")
			val res = exchangeService.getStatusOfOrderExchange(user = guid, order = orderHash)
			if (res.isSuccessful) {
				VLog.d("Result of exchange status request : ${res.body()}")
				if (res.body()?.success == true)
					return res.body()
				return null
			} else {
				VLog.d("Request is not success in getting order status : ${res.message()}")
			}
		} catch (ex: Exception) {
			VLog.d("Exception occurred in getting order status : ${ex.message}")
		}
		return null
	}


}
