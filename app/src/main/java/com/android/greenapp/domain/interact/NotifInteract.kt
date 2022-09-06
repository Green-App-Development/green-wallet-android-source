package com.android.greenapp.domain.interact

import com.android.greenapp.domain.entity.notification.NotifSection
import com.android.greenapp.presentation.tools.Status

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