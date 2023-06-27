package com.green.wallet.domain.usecases.tibet

import com.green.wallet.domain.interact.TibetInteract
import com.green.wallet.presentation.tools.Resource
import javax.inject.Inject

class PushingOfferToTibet @Inject constructor(
	private val tibetInteract: TibetInteract
) {

	suspend operator fun invoke(pair: String, offer: String): Resource<String> {
		return tibetInteract.pushOfferToTibet(pair, offer)
	}

}
