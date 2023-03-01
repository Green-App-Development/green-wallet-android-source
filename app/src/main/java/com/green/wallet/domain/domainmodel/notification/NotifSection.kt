package com.green.wallet.domain.domainmodel.notification

class NotifSection(
    val pattern: String,
    val actualTime: Long,
    val notifs: MutableList<NotificationItem>
)
