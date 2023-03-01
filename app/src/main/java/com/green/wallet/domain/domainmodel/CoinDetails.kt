package com.green.wallet.domain.domainmodel


data class CoinDetails(
    val blockchain_name: String,
    val name: String,
    val code: String,
    val description: String,
    val characteristics: String,
    val fee_commission:Double
)
