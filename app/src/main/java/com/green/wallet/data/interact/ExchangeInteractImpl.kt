package com.green.wallet.data.interact

import com.green.wallet.data.local.OrderExchangeDao
import com.green.wallet.data.local.entity.OrderEntity
import com.green.wallet.data.network.ExchangeService
import com.green.wallet.data.preference.PrefsManager
import com.green.wallet.domain.domainmodel.ExchangeRate
import com.green.wallet.domain.interact.ExchangeInteract
import com.green.wallet.domain.interact.PrefsInteract
import com.green.wallet.presentation.custom.parseException
import com.green.wallet.presentation.tools.OrderStatus
import com.green.wallet.presentation.tools.Resource
import com.green.wallet.presentation.tools.VLog
import javax.inject.Inject

class ExchangeInteractImpl @Inject constructor(
	private val exchangeService: ExchangeService,
	private val prefsInteract: PrefsInteract,
	private val orderExchangeDao: OrderExchangeDao
) : ExchangeInteract {


	override suspend fun createExchangeRequest(
		give_address: String,
		give_amount: Double,
		get_address: String,
		get_coin: String
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
						status = OrderStatus.InProgress,
						amount_to_send = give_amount,
						give_address = give_address,
						code = "XCH",
						System.currentTimeMillis()
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
				val resExchange = when (fromToken) {
					"USDT" -> {
						ExchangeRate(
							min = res.fromUSDT.toXCH.min.toDoubleOrNull() ?: 0.0,
							max = res.fromUSDT.toXCH.max.toDoubleOrNull() ?: 0.0,
							give_address = res.fromUSDT.address,
							rateXCH = res.fromUSDT.toXCH.rate.toDoubleOrNull() ?: 0.0,
							rateUSDT = res.fromXCH.toUSDT.rate.toDoubleOrNull() ?: 0.0
						)
					}

					else -> {
						ExchangeRate(
							min = res.fromXCH.toUSDT.min.toDoubleOrNull() ?: 0.0,
							max = res.fromXCH.toUSDT.max.toDoubleOrNull() ?: 0.0,
							give_address = res.fromXCH.address,
							rateXCH = res.fromUSDT.toXCH.rate.toDoubleOrNull() ?: 0.0,
							rateUSDT = res.fromXCH.toUSDT.rate.toDoubleOrNull() ?: 0.0
						)
					}
				}
				return Resource.success(resExchange)
			} else {
				VLog.d("Request is not success for exchange rate : ${request.message()}")
			}
		} catch (ex: Exception) {
			VLog.d("Exception caught in getting exchange request : ${ex.message}")
			return Resource.error(ex)
		}
		return Resource.error(Exception("Unknown error"))
	}


}
