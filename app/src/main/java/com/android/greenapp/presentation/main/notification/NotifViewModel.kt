package com.android.greenapp.presentation.main.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.greenapp.domain.domainmodel.notification.NotifSection
import com.android.greenapp.domain.interact.NotifInteract
import com.android.greenapp.presentation.tools.Status
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by bekjan on 27.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class NotifViewModel @Inject constructor(private val notifInteract: NotifInteract) : ViewModel() {

    private val _notifSectionItems = MutableStateFlow<List<NotifSection>?>(null)
    var notifSectionItems = _notifSectionItems.asStateFlow()

    private var updateJob: Job? = null

    init {
        updateQuery(null, null, null, null, null)
    }

    fun updateQuery(
        amount: Double?,
        status: Status?,
        at_least_created_time: Long?,
        yesterday: Long?,
        today: Long?
    ) {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            val notifList = notifInteract.getReadyNotifSectionWithNotifItems(
                amount,
                status,
                at_least_created_time,
                yesterday,
                today
            )
            _notifSectionItems.emit(notifList)
        }
    }


}