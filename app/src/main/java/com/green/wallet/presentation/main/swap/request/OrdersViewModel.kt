package com.green.wallet.presentation.main.swap.request

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.green.wallet.domain.domainmodel.OrderItem
import com.green.wallet.domain.interact.ExchangeInteract
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class OrdersViewModel @Inject constructor(
	private val exchangeInteract: ExchangeInteract
) : ViewModel() {

	private val _orderList = MutableStateFlow<List<OrderItem>>(emptyList())
	val orderList = _orderList.asStateFlow()

	init {
		retrievingAllOrderList()
	}

	private fun retrievingAllOrderList() {
		viewModelScope.launch {
			exchangeInteract.getAllOrderEntityList().collect {
				_orderList.emit(it)
			}
		}
	}

}
