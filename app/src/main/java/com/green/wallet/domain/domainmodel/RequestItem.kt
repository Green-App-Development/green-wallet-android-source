package com.green.wallet.domain.domainmodel

import com.green.wallet.presentation.tools.OrderStatus

data class RequestItem(
    var id: String,
    var status: OrderStatus,
    var send: Double,
    var receive: Double,
    var time_created: Long
)
