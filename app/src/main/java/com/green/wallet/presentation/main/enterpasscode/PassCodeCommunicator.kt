package com.green.wallet.presentation.main.enterpasscode

import com.green.wallet.presentation.di.application.AppScope
import com.green.wallet.presentation.tools.ReasonEnterCode
import javax.inject.Inject


@AppScope
class PassCodeCommunicator @Inject constructor() {

    var onSuccessPassCode: (ReasonEnterCode) -> Unit = {}

}