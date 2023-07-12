package com.green.wallet.presentation.main.swap.tibetliquiditydetail

import androidx.lifecycle.ViewModel
import com.green.wallet.domain.interact.ExchangeInteract
import javax.inject.Inject

class TibetLiquidityDetailViewModel @Inject constructor(
	private val exchangeInteract: ExchangeInteract
) : ViewModel() {


	fun getTibetLiquidityByOfferId(offer_id: String) =
		exchangeInteract.getTibetLiquidityDetailByOfferId(offer_id)

}
