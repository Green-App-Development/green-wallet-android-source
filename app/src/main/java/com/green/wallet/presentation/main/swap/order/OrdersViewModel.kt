package com.green.wallet.presentation.main.swap.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.green.wallet.domain.domainmodel.OrderItem
import com.green.wallet.domain.interact.ExchangeInteract
import com.green.wallet.presentation.tools.VLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class OrdersViewModel @Inject constructor(
	private val exchangeInteract: ExchangeInteract
) : ViewModel() {

	private val _exchangeList = MutableStateFlow<List<Any>>(emptyList())
	val exchangeList = _exchangeList.asStateFlow()

	init {
		VLog.d("On Create of orders vm : $this")
		retrievingAllOrderList()
	}

	private fun retrievingAllOrderList() {
		viewModelScope.launch {
			exchangeInteract.getAllOrderListFlow().collect {
				_exchangeList.emit(it)
			}
		}
	}


	override fun onCleared() {
		super.onCleared()
		VLog.d("On cleared of orders vm : $this")
	}

}
