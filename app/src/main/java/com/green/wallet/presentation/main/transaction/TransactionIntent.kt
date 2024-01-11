package com.green.wallet.presentation.main.transaction

import com.green.wallet.domain.domainmodel.Transaction

sealed interface TransactionIntent {

    data class OnSpeedyTran(val tran: Transaction) : TransactionIntent

    data class OnDeleteTransaction(val tran: Transaction) : TransactionIntent

    data class DeleteTransaction(val tran: Transaction) : TransactionIntent

}