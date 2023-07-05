package com.green.wallet.domain.interact

import com.green.wallet.domain.domainmodel.TibetLiquidityExchange
import com.green.wallet.domain.domainmodel.TibetSwapResponse
import com.green.wallet.presentation.tools.Resource

interface TibetInteract {

	suspend fun saveTokensPairID()

	suspend fun calculateAmountInAndOut(
		amountIn: Long,
		isXCH: Boolean,
		pairId: String
	): Resource<TibetSwapResponse>

	suspend fun pushOfferToTibet(pair: String, offer: String): Resource<String>

	suspend fun getTibetLiquidity(pairId: String): TibetLiquidityExchange?

}
