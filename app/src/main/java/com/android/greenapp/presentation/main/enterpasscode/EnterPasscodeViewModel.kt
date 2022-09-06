package com.android.greenapp.presentation.main.enterpasscode

import androidx.lifecycle.ViewModel
import com.android.greenapp.data.preference.PrefsManager
import com.android.greenapp.domain.interact.PrefsInteract
import javax.inject.Inject

/**
 * Created by bekjan on 29.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class EnterPasscodeViewModel @Inject constructor(private val prefsInteract: PrefsInteract) :
    ViewModel() {

    suspend fun getSetPasscode() = prefsInteract.getSettingString(PrefsManager.PASSCODE, "")

}