package com.green.wallet.domain.interact

import com.green.wallet.domain.domainmodel.notification.NotifSection
import com.green.wallet.presentation.tools.Status


interface NotifInteract {


    suspend fun getReadyNotifSectionWithNotifItems(
        amount: Double?,
        status: Status?,
        at_least_created_time: Long?,
        yesterday: Long?,
        today: Long?
    ): List<NotifSection>

}
