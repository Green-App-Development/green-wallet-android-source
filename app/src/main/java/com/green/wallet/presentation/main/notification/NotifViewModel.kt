package com.green.wallet.presentation.main.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.green.wallet.domain.domainmodel.NFTInfo
import com.green.wallet.domain.domainmodel.notification.NotifSection
import com.green.wallet.domain.interact.NFTInteract
import com.green.wallet.domain.interact.NotifInteract
import com.green.wallet.presentation.tools.Status
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class NotifViewModel @Inject constructor(
	private val notifInteract: NotifInteract,
	private val nftInfoInteract: NFTInteract
) : ViewModel() {

	private val _notifSectionItems = MutableStateFlow<List<NotifSection>?>(null)
	var notifSectionItems = _notifSectionItems.asStateFlow()

	private var updateJob: Job? = null

	private val _nftInfoState = MutableStateFlow<NFTInfo?>(null)
	val nftInfoState = _nftInfoState.asStateFlow()


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

	fun initNFTInfoByHash(hash: String) {
		viewModelScope.launch {
			val nftInfo=nftInfoInteract.getNftINFOByHash(hash)
			_nftInfoState.emit(nftInfo)
		}
	}

}
