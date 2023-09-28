package com.green.wallet.presentation.main.swap.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.green.wallet.domain.domainmodel.OrderItem
import com.green.wallet.domain.domainmodel.TibetLiquidity
import com.green.wallet.domain.domainmodel.TibetSwapExchange
import com.green.wallet.domain.interact.CryptocurrencyInteract
import com.green.wallet.domain.interact.ExchangeInteract
import com.green.wallet.domain.interact.TibetInteract
import com.green.wallet.presentation.tools.VLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class OrdersViewModel @Inject constructor(
    private val exchangeInteract: ExchangeInteract,
    private val cryptoInteract: CryptocurrencyInteract,
    private val tibetInteract: TibetInteract,
) : ViewModel() {

    private val _exchangeList = MutableStateFlow<List<Any>>(emptyList())
    val exchangeList = _exchangeList.asStateFlow()

    init {
        VLog.d("On Create of orders vm : $this")
        retrievingAllOrderList()
    }

    private fun retrievingAllOrderList() {
        viewModelScope.launch {
            exchangeInteract.getAllOrderListFlow().collect { list ->
                val sorted = list.sortedWith(object : Comparator<Any> {
                    override fun compare(p0: Any, p1: Any): Int {
                        val t1 = getTimeCreated(p0)
                        val t2 = getTimeCreated(p1)
                        return t2.compareTo(t1)
                    }
                })
                _exchangeList.emit(sorted)
            }
        }
    }

    private fun getTimeCreated(p0: Any): Long {
        return when (p0) {
            is OrderItem -> p0.timeCreated
            is TibetLiquidity -> p0.time_created
            is TibetSwapExchange -> p0.time_created
            else -> 0
        }
    }

    fun updateOrdersList(onFinish: () -> Unit) {
        viewModelScope.launch {
            exchangeInteract.updateOrderStatusPeriodically()
            exchangeInteract.updateTibetSwapExchangeStatus()
            exchangeInteract.updateTibetLiquidityStatus()
            tibetInteract.saveTokensPairID()
            onFinish.invoke()
        }
    }

    override fun onCleared() {
        super.onCleared()
        VLog.d("On cleared of orders vm : $this")
    }

}
