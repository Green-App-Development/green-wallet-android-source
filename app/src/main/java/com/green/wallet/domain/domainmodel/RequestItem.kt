package com.green.wallet.domain.domainmodel

import com.green.wallet.presentation.tools.RequestStatus

data class RequestItem(
	var id: String,
	var status: RequestStatus,
	var send: Double,
	var receive: Double,
	var time_created: Long
)
