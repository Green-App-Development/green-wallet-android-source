package com.android.greenapp.data.interact

import com.android.greenapp.data.local.TransactionDao
import com.android.greenapp.domain.entity.Transaction
import com.android.greenapp.domain.interact.TransactionInteract
import com.android.greenapp.presentation.tools.Status
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Created by bekjan on 17.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class TransactionInteractImpl @Inject constructor(private val transactionDao: TransactionDao) :
	TransactionInteract {

	override suspend fun getTransactionsByProvidedParameters(
		fkAddress: String?,
		amount: Double?,
		networkType: String?,
		status: Status?,
		at_least_created_at: Long?,
		yesterday: Long?,
		today: Long?
	) = transactionDao.getALlTransactionsByGivenParameters(
		fkAddress,
		amount,
		networkType,
		status,
		at_least_created_at,
		yesterday,
		today
	)
		.map { transaction -> transaction.toTransaction() }

	override fun getTransactionsFlowByProvidedParameters(
		fkAddress: String?,
		amount: Double?,
		networkType: String?,
		status: Status?,
		at_least_created_at: Long?,
		yesterday: Long?,
		today: Long?
	): Flow<List<Transaction>> {
		val flowTransactionList = transactionDao.getALlTransactionsFlowByGivenParameters(
			fkAddress,
			amount,
			networkType,
			status,
			at_least_created_at,
			yesterday,
			today
		).map { listFlow -> listFlow.map { tran -> tran.toTransaction() } }

		return flowTransactionList
	}


}
