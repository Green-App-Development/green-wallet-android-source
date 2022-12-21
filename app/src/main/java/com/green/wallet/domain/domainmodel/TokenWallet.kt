package com.green.wallet.domain.domainmodel

/**
 * Created by bekjan on 13.07.2022.
 * email: bekjan.omirzak98@gmail.com
 */
data class TokenWallet(
    val name: String,
    val code: String,
    val amount: Double,
    val amountInUSD: Double,
    val logo_ur: String,
	val asset_id:String
)
