package com.green.wallet.domain.usecases.tibet

import com.green.wallet.domain.domainmodel.TibetSwapExchange
import com.green.wallet.domain.interact.ExchangeInteract
import com.green.wallet.domain.interact.SpentCoinsInteract
import com.green.wallet.domain.interact.TibetInteract
import com.green.wallet.presentation.tools.ACTION_SWAP
import com.green.wallet.presentation.tools.OrderStatus
import com.green.wallet.presentation.tools.Resource
import javax.inject.Inject

class PushingOfferXCHCATToTibet @Inject constructor(
	private val tibetInteract: TibetInteract,
	private val exchangeInteract: ExchangeInteract,
	private val spentCoinsInteract: SpentCoinsInteract
) {

	suspend operator fun invoke(
		pair: String,
		offer: String,
		amountFrom: Double,
		amountTo: Double,
		catCode: String,
		isInputXCH: Boolean,
		fee: Double,
		spentXCHCoins: String,
		fk_address: String,
		donation_amount: Double
	): Resource<String> {
		val res = tibetInteract.pushOfferToTibet(pair, offer, ACTION_SWAP, donation_amount)
		if (res.state == Resource.State.SUCCESS) {
			val offerId = res.data!!
			val sendTokenCode = if (isInputXCH) "XCH" else catCode
			val getTokenCode = if (isInputXCH) catCode else "XCH"
			val timeCreated = System.currentTimeMillis()
			val tibetSwapExchange = TibetSwapExchange(
				offerId,
				send_amount = amountFrom,
				receive_amount = amountTo,
				send_coin = sendTokenCode,
				receive_coin = getTokenCode,
				fee = fee,
				time_created = timeCreated,
				status = OrderStatus.InProgress,
				height = 0
			)
			spentCoinsInteract.insertSpentCoinsJson(spentXCHCoins, timeCreated, "XCH", fk_address)
			exchangeInteract.insertTibetSwap(tibetSwapExchange)
		}
		return res
	}

}
