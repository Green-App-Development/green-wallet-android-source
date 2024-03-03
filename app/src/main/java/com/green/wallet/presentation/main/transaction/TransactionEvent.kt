package com.green.wallet.presentation.main.transaction

import com.green.wallet.domain.domainmodel.Transaction

sealed interface TransactionEvent {

    data class SpeedyBtmDialog(val transaction: Transaction?) : TransactionEvent

    data class ShowWarningDeletionDialog(val transaction: Transaction) : TransactionEvent

}