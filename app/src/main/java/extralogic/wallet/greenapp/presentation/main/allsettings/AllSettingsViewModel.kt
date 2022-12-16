package extralogic.wallet.greenapp.presentation.main.allsettings

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import extralogic.wallet.greenapp.data.preference.PrefsManager
import extralogic.wallet.greenapp.domain.interact.PrefsInteract
import kotlinx.coroutines.launch
import javax.inject.Inject

class AllSettingsViewModel @Inject constructor(private val prefs: PrefsInteract) : ViewModel() {

    suspend fun getNightModeIsOn() =
        prefs.getSettingBoolean(PrefsManager.NIGHT_MODE_ON, default = true)

    suspend fun getPushNotifIsOn() =
        prefs.getSettingBoolean(PrefsManager.PUSH_NOTIF_IS_ON, default = true)

    suspend fun getHideBalanceIsOn() =
        prefs.getSettingBoolean(PrefsManager.BALANCE_IS_HIDDEN, default = false)


    fun saveNightModeIsOn(nightMode: Boolean) {
        viewModelScope.launch {
            prefs.saveSettingBoolean(PrefsManager.NIGHT_MODE_ON, nightMode)
            AppCompatDelegate.setDefaultNightMode(if (nightMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    fun updatePushNotifIsOn(notif: Boolean) {
        viewModelScope.launch {
            prefs.saveSettingBoolean(PrefsManager.PUSH_NOTIF_IS_ON, notif)
        }
    }

    fun updateHideBalanceIsOn(hide: Boolean) {
        viewModelScope.launch {
            prefs.saveSettingBoolean(PrefsManager.BALANCE_IS_HIDDEN, hide)
        }
    }


}