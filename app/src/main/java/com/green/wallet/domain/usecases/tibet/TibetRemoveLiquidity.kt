package com.green.wallet.domain.usecases.tibet

import com.green.wallet.domain.domainmodel.TibetLiquidity
import com.green.wallet.domain.interact.ExchangeInteract
import com.green.wallet.domain.interact.SpentCoinsInteract
import com.green.wallet.domain.interact.TibetInteract
import com.green.wallet.presentation.tools.ACTION_REMOVE_LIQUIDITY
import com.green.wallet.presentation.tools.Resource
import javax.inject.Inject

class TibetRemoveLiquidity @Inject constructor(
	private val tibetInteract: TibetInteract,
	private val spentCoinsInteract: SpentCoinsInteract,
	private val exchangeInteract: ExchangeInteract
) {

	suspend operator fun invoke(
		offer: String,
		pairId: String,
		xchCoins: String,
		liquidityCoins: String,
		address: String,
		tibetLiquidity: TibetLiquidity
	): Resource<String> {
		val res = tibetInteract.pushOfferToTibetLiquidity(pairId, offer, ACTION_REMOVE_LIQUIDITY)
		if (res.state == Resource.State.SUCCESS) {
			val timeCreated = System.currentTimeMillis()
			tibetLiquidity.time_created = timeCreated
			tibetLiquidity.offer_id = res.data!!
			spentCoinsInteract.insertSpentCoinsJson(
				liquidityCoins,
				timeCreated,
				tibetLiquidity.liquidityToken,
				address
			)
			spentCoinsInteract.insertSpentCoinsJson(xchCoins, timeCreated, "XCH", address)
			tibetInteract.insertTibetLiquidity(tibetLiquidity)
		}
		return res
	}

}
