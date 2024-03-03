package com.green.wallet.presentation.main.transaction

import com.green.wallet.domain.domainmodel.OfferTransaction
import com.green.wallet.domain.domainmodel.TransferTransaction

sealed interface TransactionEvent {

    data class SpeedyBtmDialog(val transaction: TransferTransaction?) : TransactionEvent
    data class ShowTransactionDetails(val transaction: TransferTransaction) : TransactionEvent

    data class ShowWarningDeletionDialog(val transaction: TransferTransaction) : TransactionEvent

    data class ShowBtmDialogCancelOffer(val transaction: OfferTransaction) : TransactionEvent

}