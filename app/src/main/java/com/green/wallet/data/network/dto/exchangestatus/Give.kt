package com.green.wallet.data.network.dto.exchangestatus

data class Give(
    val address: String,
    val amount: String,
    val coin: String,
    val txID: String?
)
