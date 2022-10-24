package com.android.greenapp.presentation.onboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.greenapp.data.network.dto.greenapp.language.LanguageItem
import com.android.greenapp.data.preference.PrefsManager
import com.android.greenapp.domain.interact.GreenAppInteract
import com.android.greenapp.domain.interact.PrefsInteract
import com.android.greenapp.presentation.custom.NotificationHelper
import com.android.greenapp.presentation.custom.isExceptionBelongsToNoInternet
import com.android.greenapp.presentation.tools.Resource
import com.example.common.tools.VLog
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject


class OnBoardViewModel @Inject constructor(
    private val prefs: PrefsInteract,
    private val greenAppInteract: GreenAppInteract,
    private val gson: Gson,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    private val _languageList =
        MutableStateFlow<Resource<List<LanguageItem>>>(Resource.loading())
    val languageList: StateFlow<Resource<List<LanguageItem>>> = _languageList

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

    fun downloadLanguage(langCode: String) {
        downloadLangJob?.cancel()
        downloadLangJob = viewModelScope.launch {
            kotlin.runCatching {
                _downloadingLang.emit(Resource.loading())
                val res=greenAppInteract.downloadLanguageTranslate(langCode = langCode)
                _downloadingLang.emit(res)
            }.onFailure {
                _downloadingLang.emit(Resource.error(Exception(it.message)))
            }
        }
    }

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

    fun saveAppStartTime(value: Long) {
        viewModelScope.launch {
            prefs.saveSettingLong(PrefsManager.APP_START_TIME, value)
        }
    }


}