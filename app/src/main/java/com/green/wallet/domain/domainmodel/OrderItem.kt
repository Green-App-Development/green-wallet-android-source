package com.green.wallet.domain.domainmodel

import android.os.Parcelable
import com.green.wallet.presentation.tools.OrderStatus
import kotlinx.android.parcel.Parcelize


@Parcelize
data class OrderItem(
    val hash: String,
    val amountToSend: Double,
    val giveAddress: String,
    val timeCreated: Long,
    val status: OrderStatus,
    val rate: Double,
    val getCoin: String,
    val sendCoin: String,
    val getAddress: String,
    val txID: String,
    val commission_fee: Double,
    val amountToReceive: Double,
    val commission_tron: Double,
    val commission_percent: Double
) : Parcelable