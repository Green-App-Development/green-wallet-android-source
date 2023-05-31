package com.green.wallet.domain.interact

import com.green.wallet.domain.domainmodel.ExchangeRate
import com.green.wallet.presentation.tools.Resource

interface ExchangeInteract {

	suspend fun createExchangeRequest(
		give_address: String,
		give_amount: Double,
		get_address: String,
		get_coin: String
	): Resource<String>

	suspend fun getExchangeRequest(fromToken: String): Resource<ExchangeRate>


}
