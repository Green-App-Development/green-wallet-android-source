package extralogic.wallet.greenapp.presentation.main.language

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import extralogic.wallet.greenapp.data.network.dto.greenapp.lang.LanguageItemDto
import extralogic.wallet.greenapp.domain.interact.GreenAppInteract
import extralogic.wallet.greenapp.presentation.custom.isExceptionBelongsToNoInternet
import extralogic.wallet.greenapp.presentation.tools.Resource
import com.example.common.tools.VLog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    private val greenAppInteract: GreenAppInteract
) : ViewModel() {

    private val _languageList =
        MutableStateFlow<Resource<List<LanguageItemDto>>>(Resource.loading())
    val languageList: StateFlow<Resource<List<LanguageItemDto>>> = _languageList

    private val _downloadingLang = MutableStateFlow<Resource<String>?>(null)
    val downloadingLang = _downloadingLang.asStateFlow()

    private val handler = CoroutineExceptionHandler { _, ex ->
        VLog.d("Exception occurred in getting lang list")
    }
    private var downloadJob: Job? = null
    private var langListJob: Job? = null


   suspend fun downloadLanguage(langCode: String)=greenAppInteract.downloadLanguageTranslate(langCode)


    fun getAllLanguageList(times:Int) {
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


}
