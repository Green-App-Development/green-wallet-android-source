package com.android.greenapp.domain.domainmodel.notification

/**
 * Created by bekjan on 31.05.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class NotifSection(
    val pattern: String,
    val actualTime: Long,
    val notifs: MutableList<NotificationItem>
)