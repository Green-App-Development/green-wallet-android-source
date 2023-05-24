package com.green.wallet.presentation.onboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.green.wallet.data.network.dto.greenapp.lang.LanguageItemDto
import com.green.wallet.data.preference.PrefsManager
import com.green.wallet.domain.interact.GreenAppInteract
import com.green.wallet.domain.interact.PrefsInteract
import com.green.wallet.presentation.custom.isExceptionBelongsToNoInternet
import com.green.wallet.presentation.tools.Resource
import com.green.wallet.presentation.tools.VLog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


class OnBoardViewModel @Inject constructor(
	private val prefs: PrefsInteract,
	private val greenAppInteract: GreenAppInteract
) : ViewModel() {

	private val _languageList =
		MutableStateFlow<Resource<List<LanguageItemDto>>>(Resource.loading())
	val languageList: StateFlow<Resource<List<LanguageItemDto>>> = _languageList

	private val _downloadingLang = MutableStateFlow<Resource<String>?>(null)
	val downloadingLang = _downloadingLang.asStateFlow()

	private val handler = CoroutineExceptionHandler { _, ex ->
		VLog.d("Exception occurred in getting lang list : ${ex.message}")
	}


	init {
//        notificationHelper.buildingNotificationChannels()
	}


	private var downloadLangJob: Job? = null
	private var langListJob: Job? = null

	suspend fun getGreetingAgreementText() = greenAppInteract.getAgreementsText()

	suspend fun downloadLanguage(langCode: String) =
		greenAppInteract.downloadLanguageTranslate(langCode)

	fun saveUserUnBoarded(boarded: Boolean) {
		viewModelScope.launch {
			prefs.saveSettingBoolean(PrefsManager.USER_UNBOARDED, boarded)
		}
	}

	fun savePasscode(password: String) {
		viewModelScope.launch {
			prefs.saveSettingString(PrefsManager.PASSCODE, password)
		}
	}

	fun saveLastVisitedValue(value: Long) {
		viewModelScope.launch {
			prefs.saveSettingLong(PrefsManager.LAST_VISITED, value = value)
		}
	}

	fun getAllLanguageList(times: Int) {
		langListJob?.cancel()
		langListJob = viewModelScope.launch {
			val res = greenAppInteract.getAvailableLanguageList()
			_languageList.emit(res)
			if (times != 0 && res.state == Resource.State.ERROR && isExceptionBelongsToNoInternet(
					res.error!!
				)
			) {
				delay(2500)
				getAllLanguageList(times - 1)
			}
		}
	}

	fun saveAppInstallTime(value: Long) {
		viewModelScope.launch {
			prefs.saveSettingLong(PrefsManager.APP_INSTALL_TIME, value)
		}
	}


}
