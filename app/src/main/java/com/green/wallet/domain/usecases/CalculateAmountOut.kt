package com.green.wallet.domain.usecases

import com.green.wallet.domain.domainmodel.TibetSwap
import com.green.wallet.domain.interact.TibetInteract
import com.green.wallet.presentation.tools.Resource
import javax.inject.Inject

class CalculateAmountOut @Inject constructor(
	private val tibetInteract: TibetInteract
) {

	suspend operator fun invoke(
		amountIn: Double,
		isXCH: Boolean,
		pairID: String
	): Resource<TibetSwap> {
		val amount: Long
		if (isXCH)
			amount = (amountIn * Math.pow(10.0, 12.0)).toLong()
		else
			amount = (amountIn * Math.pow(10.0, 3.0)).toLong()
		return tibetInteract.calculateAmountInAndOut(amount, isXCH, pairID)
	}


}
