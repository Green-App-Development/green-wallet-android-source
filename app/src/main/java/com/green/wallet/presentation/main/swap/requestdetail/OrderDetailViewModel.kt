package com.green.wallet.presentation.main.swap.requestdetail

import androidx.lifecycle.ViewModel
import com.green.wallet.domain.interact.ExchangeInteract
import javax.inject.Inject

class OrderDetailViewModel @Inject constructor(
	private val exchangeInteract: ExchangeInteract
) : ViewModel() {


	suspend fun getOrderItemByHash(hash: String) = exchangeInteract.getOrderByHash(hash)


}
