package com.green.wallet.presentation.main.pincode

import androidx.lifecycle.ViewModel
import com.green.wallet.data.preference.PrefsManager
import com.green.wallet.domain.interact.PrefsInteract
import com.green.wallet.presentation.tools.ReasonEnterCode
import javax.inject.Inject


class PinCodeViewModel @Inject constructor(
    private val prefsInteract: PrefsInteract,
    private val passCodeCommunicator: PinCodeCommunicator
) :
    ViewModel() {

    suspend fun getSetPasscode() = prefsInteract.getSettingString(PrefsManager.PASSCODE, "")

    fun setPassCode(reasonEnterCode: ReasonEnterCode) {
        passCodeCommunicator.onSuccessPassCode.invoke(reasonEnterCode)
    }

}
