package com.green.wallet.domain.interact

import com.green.wallet.domain.domainmodel.Transaction
import com.green.wallet.presentation.tools.Status


interface TransactionInteract {

	suspend fun getTransactionsByProvidedParameters(
		fkAddress: String?,
		amount: Double?,
		networkType: String?,
		status: Status?,
		at_least_created_at: Long?,
		yesterday: Long?,
		today: Long?
	): List<Transaction>

	fun getTransactionsFlowByProvidedParameters(
		fkAddress: String?,
		amount: Double?,
		networkType: String?,
		status: Status?,
		at_least_created_at: Long?,
		yesterday: Long?,
		today: Long?,
		tokenCode:String?
	): kotlinx.coroutines.flow.Flow<List<Transaction>>

	suspend fun getMempoolTransactionsAmountByAddressAndToken(
		address: String,
		token: String,
		networkType: String
	): DoubleArray

}
