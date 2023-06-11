package com.green.wallet.data.network.dto.exchangestatus

data class Result(
    val created_at: String,
    val ended_at: String?,
    val fee: String,
    val `get`: Get,
    val give: Give,
    val order: String,
    val rate: String,
    val status: String
)
