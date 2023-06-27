package com.green.wallet.domain.interact

import com.green.wallet.domain.domainmodel.TibetSwap
import com.green.wallet.presentation.tools.Resource

interface TibetInteract {

	suspend fun saveTokensPairID()

	suspend fun calculateAmountInAndOut(
		amountIn: Long,
		isXCH: Boolean,
		pairId: String
	): Resource<TibetSwap>

	suspend fun pushOfferToTibet(pair: String, offer: String): Resource<String>


}
