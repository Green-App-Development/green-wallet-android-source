package com.android.greenapp.data.interact

import com.android.greenapp.data.local.TransactionDao
import com.android.greenapp.domain.interact.TransactionInteract
import com.android.greenapp.presentation.tools.Status
import javax.inject.Inject

/**
 * Created by bekjan on 17.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class TransactionInteractImpl @Inject constructor(private val transactionDao: TransactionDao) :
    TransactionInteract {

    override suspend fun getTransactionsFlowByProvidedParameters(
        fingerPrint:Long?,
        amount: Double?,
        networkType: String?,
        status: Status?,
        at_least_created_at: Long?,
        yesterday: Long?,
        today: Long?
    ) = transactionDao.getALlTransactionsByGivenParameters(
        fingerPrint,
        amount,
        networkType,
        status,
        at_least_created_at,
        yesterday,
        today
    )
        .map {transaction -> transaction.toTransaction()}


}