package com.green.wallet.presentation.main.pincode

import com.green.wallet.presentation.di.application.AppScope
import com.green.wallet.presentation.tools.ReasonEnterCode
import javax.inject.Inject


@AppScope
class PinCodeCommunicator @Inject constructor() {

    var onSuccessPassCode: (ReasonEnterCode) -> Unit = {}

}