package com.green.wallet.domain.interact

import com.green.wallet.domain.domainmodel.notification.NotifSection
import com.green.wallet.presentation.tools.Status

/**
 * Created by bekjan on 27.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */
interface NotifInteract {


    suspend fun getReadyNotifSectionWithNotifItems(
        amount: Double?,
        status: Status?,
        at_least_created_time: Long?,
        yesterday: Long?,
        today: Long?
    ): List<NotifSection>

}
