package extralogic.wallet.greenapp.domain.interact

import extralogic.wallet.greenapp.domain.domainmodel.Transaction
import extralogic.wallet.greenapp.presentation.tools.Status

/**
 * Created by bekjan on 17.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */
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
		today: Long?
	): kotlinx.coroutines.flow.Flow<List<Transaction>>

	suspend fun getMempoolTransactionsAmountByAddressAndToken(
		address: String,
		token: String,
		networkType: String
	): DoubleArray

}
