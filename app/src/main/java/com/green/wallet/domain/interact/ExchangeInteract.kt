package com.green.wallet.domain.interact

interface ExchangeInteract {

	suspend fun createExchangeRequest(
		give_address: String,
		give_amount: Double,
		get_address: String
	)


}
