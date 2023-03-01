package com.green.wallet.presentation.main.enterpasscode

import androidx.lifecycle.ViewModel
import com.green.wallet.data.preference.PrefsManager
import com.green.wallet.domain.interact.PrefsInteract
import javax.inject.Inject


class EnterPasscodeViewModel @Inject constructor(private val prefsInteract: PrefsInteract) :
    ViewModel() {

    suspend fun getSetPasscode() = prefsInteract.getSettingString(PrefsManager.PASSCODE, "")

}
