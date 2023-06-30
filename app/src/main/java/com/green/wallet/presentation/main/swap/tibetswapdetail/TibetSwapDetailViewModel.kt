package com.green.wallet.presentation.main.swap.tibetswapdetail

import androidx.lifecycle.ViewModel
import com.green.wallet.domain.interact.ExchangeInteract
import javax.inject.Inject

class TibetSwapDetailViewModel @Inject constructor(
	private val exchangeInteract: ExchangeInteract
) : ViewModel() {


	fun getTibetSwapExchangeOfferId(offerId: String) =
		exchangeInteract.getTibetSwapDetailByOfferId(offerId)


}
