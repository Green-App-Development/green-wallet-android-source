package com.green.wallet.domain.domainmodel.notification

import com.green.wallet.presentation.tools.Status

/**
 * Created by bekjan on 31.05.2022.
 * email: bekjan.omirzak98@gmail.com
 */
data class NotificationItem(
    val status: Status = Status.Incoming,
    val amount: Double=0.0,
    val amountInUSD: Double=0.0,
    val networkType: String="",
    val height: Long=0L,
    val commission: Double=0.0,
    val created_at_time: Long=0L,
    val message: String="",
	val code:String=""
)
