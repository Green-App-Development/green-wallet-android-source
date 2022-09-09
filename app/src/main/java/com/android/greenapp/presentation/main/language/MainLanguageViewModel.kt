package com.android.greenapp.presentation.main.language

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.greenapp.data.network.dto.greenapp.language.LanguageItem
import com.android.greenapp.data.preference.PrefsManager
import com.android.greenapp.domain.interact.GreenAppInteract
import com.android.greenapp.domain.interact.PrefsInteract
import com.android.greenapp.presentation.tools.Resource
import com.example.common.tools.VLog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by bekjan on 11.05.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class MainLanguageViewModel @Inject constructor(
	private val prefs: PrefsInteract,
	private val languageInteract: GreenAppInteract
) : ViewModel() {

	private val _languageList =
		MutableStateFlow<Resource<List<LanguageItem>>>(Resource.loading())
	val languageList: StateFlow<Resource<List<LanguageItem>>> = _languageList

	private val _downloadingLang = MutableStateFlow<Resource<String>?>(null)
	val downloadingLang = _downloadingLang.asStateFlow()

	private val handler = CoroutineExceptionHandler { _, ex ->
		VLog.d("Exception occurred in getting lang list")
	}
	private var downloadJob: Job? = null

	suspend fun getVersionRequest() = prefs.getSettingString(PrefsManager.VERSION_REQUEST, "")

	fun downloadLanguage(langCode: String) {
		downloadJob?.cancel()
		downloadJob = viewModelScope.launch {
			_downloadingLang.emit(Resource.loading())
			val res = languageInteract.downloadLanguageTranslate(langCode = langCode)
			_downloadingLang.emit(res)
		}
	}


	fun getAllLanguageList() {
		viewModelScope.launch {
			val res = languageInteract.getAvailableLanguageList()
			_languageList.emit(res)
		}
	}


}
