package com.green.wallet.presentation.intro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.green.wallet.data.local.AppDatabase
import com.green.wallet.data.preference.PrefsManager
import com.green.wallet.domain.interact.CryptocurrencyInteract
import com.green.wallet.domain.interact.GreenAppInteract
import com.green.wallet.domain.interact.PrefsInteract
import com.green.wallet.domain.interact.SupportInteract
import com.green.wallet.domain.interact.TibetInteract
import com.green.wallet.presentation.tools.VLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class IntroActViewModel @Inject constructor(
	private val prefs: PrefsInteract,
	private val appDatabase: AppDatabase,
	private val greenAppInteract: GreenAppInteract,
	private val supportInteract:SupportInteract,
	private val cryptocurrencyInteract: CryptocurrencyInteract,
	private val tibetInteract: TibetInteract
) : ViewModel() {

	private var clearingJob: Job? = null

	init {
		viewModelScope.launch {
			val serverTime = greenAppInteract.getServerTime()
			val timeDifference = System.currentTimeMillis() - serverTime
			VLog.d("Time Difference : $timeDifference is saved in settings on Authenticate")
			prefs.saveSettingLong(PrefsManager.TIME_DIFFERENCE, timeDifference)
		}
		requestPerApplication()
	}

	private fun requestPerApplication(){
		viewModelScope.launch {
			with(greenAppInteract) {
				getAvailableNetworkItemsFromRestAndSave()
				getAvailableLanguageList()
				getVerifiedDidList()
				getAgreementsText()
				updateCoinDetails()
			}
			supportInteract.getFAQQuestionAnswers()
			with(cryptocurrencyInteract) {
				getAllTails()
				checkingDefaultWalletTails()
			}
			with(tibetInteract) {
				saveTokensPairID()
			}
		}
	}

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
