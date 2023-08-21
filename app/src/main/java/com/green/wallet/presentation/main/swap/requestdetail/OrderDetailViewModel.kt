package com.green.wallet.presentation.main.swap.requestdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.green.wallet.domain.interact.ExchangeInteract
import com.green.wallet.presentation.tools.OrderStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class OrderDetailViewModel @Inject constructor(
    private val exchangeInteract: ExchangeInteract
) : ViewModel() {


    fun getOrderItemByHash(hash: String) = exchangeInteract.getOrderByHash(hash)
    fun updateOrderStatus(hash: String, status: OrderStatus) {
        viewModelScope.launch {
            exchangeInteract.updateOrderStatusByHash(hash, status)
        }
    }

    fun updateOrdersStatus(done: () -> Unit) {
        viewModelScope.launch {
            exchangeInteract.updateOrderStatusPeriodically()
            done()
        }
    }


}
