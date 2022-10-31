package com.android.greenapp.domain.interact

import com.android.greenapp.domain.entity.Transaction
import com.android.greenapp.presentation.tools.Status
import java.util.concurrent.Flow

/**
 * Created by bekjan on 17.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */
interface TransactionInteract {

	suspend fun getTransactionsByProvidedParameters(
		fingerPrint: Long?,
		amount: Double?,
		networkType: String?,
		status: Status?,
		at_least_created_at: Long?,
		yesterday: Long?,
		today: Long?
	): List<Transaction>

	fun getTransactionsFlowByProvidedParameters(
		fingerPrint: Long?,
		amount: Double?,
		networkType: String?,
		status: Status?,
		at_least_created_at: Long?,
		yesterday: Long?,
		today: Long?
	): kotlinx.coroutines.flow.Flow<List<Transaction>>


}
