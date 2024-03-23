package com.green.wallet.presentation.main.transaction

import com.green.wallet.domain.domainmodel.Transaction

data class TransactionState(
    val isLoading: Boolean = false,
    val transactionList: List<Transaction> = listOf()
)