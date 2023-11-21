package com.green.wallet.presentation.main.enterpasscode

import androidx.lifecycle.ViewModel
import com.green.wallet.data.preference.PrefsManager
import com.green.wallet.domain.interact.PrefsInteract
import com.green.wallet.presentation.custom.encryptor.Encryptor
import com.green.wallet.presentation.custom.encryptor.EncryptorProvider
import javax.inject.Inject


class EnterPasscodeViewModel @Inject constructor(
    private val prefsInteract: PrefsInteract,
    val encryptor: EncryptorProvider
) :
    ViewModel() {

    suspend fun getSetPasscode() = prefsInteract.getSettingString(PrefsManager.PASSCODE, "")

}
