package com.green.wallet.domain.usecases.tibet

import com.green.wallet.domain.domainmodel.OrderItem
import com.green.wallet.domain.interact.OrderExchangeInteract
import com.green.wallet.domain.interact.TibetInteract
import com.green.wallet.presentation.tools.OrderStatus
import com.green.wallet.presentation.tools.OrderType
import com.green.wallet.presentation.tools.Resource
import javax.inject.Inject

class PushingOfferToTibet @Inject constructor(
	private val tibetInteract: TibetInteract,
	private val orderExchangeInteract: OrderExchangeInteract
) {

	suspend operator fun invoke(
		pair: String,
		offer: String,
		amountFrom: Double,
		amountTo: Double,
		catCode: String,
		isInputXCH: Boolean
	): Resource<String> {
		val res = tibetInteract.pushOfferToTibet(pair, offer)
		if (res.state == Resource.State.SUCCESS) {
			val offerId = res.data!!
			val orderItem = OrderItem(
				hash = offerId,
				amountFrom,
				giveAddress = "",
				System.currentTimeMillis(),
				status = OrderStatus.InProgress,
				rate = 0.0,
				getCoin = "",
				sendCoin = "",
				getAddress = "",
				txID = "",
				fee = 0.0,
				orderType = OrderType.XCH_CAT
			)
			orderExchangeInteract.insertOrderItem(orderItem)
		}
		return res
	}

}
