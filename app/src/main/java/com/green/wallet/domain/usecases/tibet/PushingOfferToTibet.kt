package com.green.wallet.domain.usecases.tibet

import com.green.wallet.domain.domainmodel.TibetSwapExchange
import com.green.wallet.domain.interact.ExchangeInteract
import com.green.wallet.domain.interact.TibetInteract
import com.green.wallet.presentation.tools.OrderStatus
import com.green.wallet.presentation.tools.Resource
import com.green.wallet.presentation.tools.Status
import javax.inject.Inject

class PushingOfferToTibet @Inject constructor(
	private val tibetInteract: TibetInteract,
	private val exchangeInteract: ExchangeInteract
) {

	suspend operator fun invoke(
		pair: String,
		offer: String,
		amountFrom: Double,
		amountTo: Double,
		catCode: String,
		isInputXCH: Boolean,
		fee: Double
	): Resource<String> {
		val res = tibetInteract.pushOfferToTibet(pair, offer)
		if (res.state == Resource.State.SUCCESS) {
			val offerId = res.data!!
			val sendTokenCode = if (isInputXCH) "XCH" else catCode
			val getTokenCode = if (isInputXCH) catCode else "XCH"
			val tibetSwapExchange = TibetSwapExchange(
				offerId,
				send_amount = amountFrom,
				receive_amount = amountTo,
				send_coin = sendTokenCode,
				receive_coin = getTokenCode,
				fee = fee,
				time_created = System.currentTimeMillis(),
				status = OrderStatus.InProgress,
				height = 0
			)
			exchangeInteract.insertTibetSwap(tibetSwapExchange)
		}
		return res
	}

}