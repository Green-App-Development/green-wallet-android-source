package com.android.greenapp.domain.entity

/**
 * Created by bekjan on 14.07.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class CoinDetails(
    val blockchain_name: String,
    val name: String,
    val code: String,
    val description: String,
    val characteristics: String,
    val fee_commission:Double
)