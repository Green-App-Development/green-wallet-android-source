package com.android.greenapp.presentation.main.transaction

import androidx.lifecycle.ViewModel
import com.android.greenapp.domain.entity.Transaction
import com.android.greenapp.domain.interact.BlockChainInteract
import com.android.greenapp.domain.interact.TransactionInteract
import com.android.greenapp.domain.interact.WalletInteract
import com.android.greenapp.presentation.tools.Status
import com.example.common.tools.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Created by bekjan on 11.05.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class TransactionsViewModel @Inject constructor(
	private val transactionInteract: TransactionInteract,
	private val blockChainInteract: BlockChainInteract,
	private val walletInteract: WalletInteract
) :
	ViewModel() {

	private val _transactionList = MutableStateFlow<List<Transaction>?>(null)
	val transactionList = _transactionList.asStateFlow()

	suspend fun getAllQueriedTransactionList(
		fingerPrint: Long?,
		amount: Double?,
		networkType: String?,
		status: Status?,
		at_least_created_at: Long?,
		yesterdayStart: Long?,
		yesterdayEnd: Long?
	): List<Transaction> {
		VLog.d(
			"FingerPrint : $fingerPrint  Amount : $amount and networktype : $networkType and status : $status at_least_created_time : ${
				formattedTime(
					at_least_created_at ?: 0
				)
			}  yesterdayStart : ${formattedTime(yesterdayStart ?: 0)}  : yesterdayEnd : ${
				formattedTime(
					yesterdayEnd ?: 0
				)
			}"
		)
		return transactionInteract.getTransactionsByProvidedParameters(
			fingerPrint,
			amount,
			networkType,
			status,
			at_least_created_at,
			yesterdayStart,
			yesterdayEnd
		)
	}


	fun getAllQueriedFlowTransactionList(
		fingerPrint: Long?,
		amount: Double?,
		networkType: String?,
		status: Status?,
		at_least_created_at: Long?,
		yesterdayStart: Long?,
		yesterdayEnd: Long?
	): Flow<List<Transaction>> {
		VLog.d(
			"FingerPrint : $fingerPrint  Amount : $amount and networktype : $networkType and status : $status at_least_created_time : ${
				formattedTime(
					at_least_created_at ?: 0
				)
			}  yesterdayStart : ${formattedTime(yesterdayStart ?: 0)}  : yesterdayEnd : ${
				formattedTime(
					yesterdayEnd ?: 0
				)
			}"
		)
		return transactionInteract.getTransactionsFlowByProvidedParameters(
			fingerPrint,
			amount,
			networkType,
			status,
			at_least_created_at,
			yesterdayStart,
			yesterdayEnd
		)
	}

	suspend fun getDistinctNetworkTypeValues() = walletInteract.getDistinctNetworkTypes()

}
