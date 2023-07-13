package com.green.wallet.domain.interact

import com.green.wallet.domain.domainmodel.TibetLiquidity
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

	suspend fun pushOfferToTibetLiquidity(
		pair: String,
		offer: String,
		action: String,
		donationAmount: Double = 0.0
	): Resource<String>

	suspend fun pushOfferToTibetCAT(
		pair: String,
		offer: String,
		action: String,
		donationAmount: Double = 0.0,
		devFee:Int,
		walletFee:Int
	): Resource<String>




	suspend fun getTibetLiquidity(pairId: String): TibetLiquidityExchange?

	suspend fun insertTibetLiquidity(tibetLiquidity: TibetLiquidity)


}
