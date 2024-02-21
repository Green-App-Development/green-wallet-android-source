package com.green.wallet.presentation.main.transaction

import com.green.wallet.domain.domainmodel.TransferTransaction

sealed interface TransactionIntent {

    data class OnSpeedyTran(val tran: TransferTransaction) : TransactionIntent

    data class OnDeleteTransaction(val tran: TransferTransaction) : TransactionIntent

    data class DeleteTransaction(val tran: TransferTransaction) : TransactionIntent

}