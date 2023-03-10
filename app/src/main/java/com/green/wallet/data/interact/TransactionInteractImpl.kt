package com.green.wallet.data.interact

import com.green.wallet.data.local.TransactionDao
import com.green.wallet.domain.domainmodel.Transaction
import com.green.wallet.domain.interact.TransactionInteract
import com.green.wallet.presentation.custom.getShortNetworkType
import com.green.wallet.presentation.tools.Status
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


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
		today: Long?,
		tokenCode: String?
	): Flow<List<Transaction>> {
		val flowTransactionList = transactionDao.getALlTransactionsFlowByGivenParameters(
			fkAddress,
			amount,
			networkType,
			status,
			at_least_created_at,
			yesterday,
			today,
			tokenCode
		).map { listFlow -> listFlow.map { tran -> tran.toTransaction() } }

		return flowTransactionList
	}

	override suspend fun getMempoolTransactionsAmountByAddressAndToken(
		address: String,
		token: String,
		networkType: String
	): DoubleArray {
		val mempoolTransByCode = transactionDao.getMempoolTransactionsByAddressAndToken(
			status = Status.InProgress,
			address = address,
			code = token
		)

		val networkTypeCode = getShortNetworkType(networkType)
		val amountByCurSendingToken = mempoolTransByCode.map {
			if (token == networkTypeCode)
				it.amount + it.fee_amount
			else
				it.amount
		}.sum()
		val amountByNetworkType = transactionDao.getMempoolTransactionsByAddressAndToken(
			status = Status.InProgress,
			address = address,
			networkTypeCode
		).map { it.amount + it.fee_amount }.sum()

		return doubleArrayOf(amountByCurSendingToken, amountByNetworkType)
	}


}
