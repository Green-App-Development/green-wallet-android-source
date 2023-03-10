package com.green.wallet.presentation.intro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.green.wallet.data.local.AppDatabase
import com.green.wallet.data.preference.PrefsManager
import com.green.wallet.domain.interact.PrefsInteract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class IntroActViewModel @Inject constructor(
	private val prefs: PrefsInteract,
	private val appDatabase: AppDatabase
) : ViewModel() {

	private var clearingJob: Job? = null

	suspend fun isUserUnBoarded() =
		prefs.getSettingBoolean(PrefsManager.USER_UNBOARDED, default = false)

	suspend fun getSavedPasscode() = prefs.getSettingString(PrefsManager.PASSCODE, "")

	suspend fun getNightModeIsOn() =
		prefs.getSettingBoolean(PrefsManager.NIGHT_MODE_ON, default = true)

	suspend fun getLastVisitedLongValue() = prefs.getSettingLong(PrefsManager.LAST_VISITED, 0L)

	suspend fun saveLastVisitedLongValue(value: Long) =
		prefs.saveSettingLong(PrefsManager.LAST_VISITED, value)

	fun clearRoomDataStore(callBack: () -> Unit) {
		clearingJob?.cancel()
		clearingJob = viewModelScope.launch(Dispatchers.IO) {
			appDatabase.clearAllTables()
			prefs.saveSettingLong(PrefsManager.APP_INSTALL_TIME, System.currentTimeMillis())
			prefs.saveSettingBoolean(PrefsManager.USER_UNBOARDED, false)
			callBack()
		}
	}

}
