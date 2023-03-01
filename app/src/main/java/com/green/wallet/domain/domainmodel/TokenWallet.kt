package com.green.wallet.domain.domainmodel


data class TokenWallet(
    val name: String,
    val code: String,
    val amount: Double,
    val amountInUSD: Double,
    val logo_ur: String,
	val asset_id:String
)
