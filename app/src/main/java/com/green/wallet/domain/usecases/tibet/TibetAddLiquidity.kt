package com.green.wallet.domain.usecases.tibet

import com.green.wallet.domain.domainmodel.TibetLiquidity
import com.green.wallet.domain.interact.ExchangeInteract
import com.green.wallet.domain.interact.SpentCoinsInteract
import com.green.wallet.domain.interact.TibetInteract
import com.green.wallet.presentation.tools.ACTION_ADD_LIQUIDITY
import com.green.wallet.presentation.tools.Resource
import javax.inject.Inject

class TibetAddLiquidity @Inject constructor(
	private val tibetInteract: TibetInteract,
	private val spentCoinsInteract: SpentCoinsInteract,
	private val exchangeInteract: ExchangeInteract
) {

	suspend operator fun invoke(
		offer: String,
		pairId: String,
		xchCoins: String,
		tokenCoins: String,
		address: String,
		tibetLiquidity: TibetLiquidity
	): Resource<String> {
		val res = tibetInteract.pushOfferToTibetLiquidity(pairId, offer, ACTION_ADD_LIQUIDITY)
		if (res.state == Resource.State.SUCCESS) {
			val offerId = res.data!!
			val timeCreated = System.currentTimeMillis()
			tibetLiquidity.offer_id = offerId
			tibetLiquidity.time_created = timeCreated
			spentCoinsInteract.insertSpentCoinsJson(xchCoins, timeCreated, "XCH", address)
			spentCoinsInteract.insertSpentCoinsJson(
				tokenCoins,
				timeCreated,
				tibetLiquidity.catToken,
				address
			)
			tibetInteract.insertTibetLiquidity(tibetLiquidity)
		}
		return res
	}


}
