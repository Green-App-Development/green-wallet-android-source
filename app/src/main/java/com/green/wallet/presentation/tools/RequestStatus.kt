package com.green.wallet.presentation.tools

import java.io.Serializable

enum class RequestStatus : Serializable {
	InProgress,
	Cancelled,
	Completed,
	Waiting
}
