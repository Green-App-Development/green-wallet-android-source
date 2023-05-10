package com.green.wallet.presentation.main.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.green.wallet.domain.domainmodel.Transaction
import com.green.wallet.domain.interact.*
import com.green.wallet.presentation.tools.Status
import com.example.common.tools.*
import com.green.wallet.domain.domainmodel.NFTInfo
import com.green.wallet.presentation.tools.VLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


class TransactionsViewModel @Inject constructor(
	private val transactionInteract: TransactionInteract,
	private val blockChainInteract: BlockChainInteract,
	private val walletInteract: WalletInteract,
	private val greenAppInteract: GreenAppInteract,
	private val nftInteract: NFTInteract
) :
	ViewModel() {

	private val _nftInfoState = MutableStateFlow<NFTInfo?>(null)
	val nftInfoState = _nftInfoState.asStateFlow()

	suspend fun getAllQueriedTransactionList(
		fkAddress: String?,
		amount: Double?,
		networkType: String?,
		status: Status?,
		at_least_created_at: Long?,
		yesterdayStart: Long?,
		yesterdayEnd: Long?
	): List<Transaction> {
		VLog.d(
			"FingerPrint : $fkAddress  Amount : $amount and networktype : $networkType and status : $status at_least_created_time : ${
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
			fkAddress,
			amount,
			networkType,
			status,
			at_least_created_at,
			yesterdayStart,
			yesterdayEnd
		)
	}


	fun getAllQueriedFlowTransactionList(
		fkAddress: String?,
		amount: Double?,
		networkType: String?,
		status: Status?,
		at_least_created_at: Long?,
		yesterdayStart: Long?,
		yesterdayEnd: Long?,
		tokenCode: String?
	): Flow<List<Transaction>> {
		VLog.d(
			"FingerPrint : $fkAddress  Amount : $amount and networktype : $networkType and status : $status at_least_created_time : ${
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
			fkAddress,
			amount,
			networkType,
			status,
			at_least_created_at,
			yesterdayStart,
			yesterdayEnd,
			tokenCode
		)
	}


	fun getAllQueriedFlowTransactionPagingSource(
		fkAddress: String?,
		amount: Double?,
		networkType: String?,
		status: Status?,
		at_least_created_at: Long?,
		yesterdayStart: Long?,
		yesterdayEnd: Long?,
		tokenCode: String?
	): Flow<PagingData<Transaction>> {
		VLog.d(
			"FingerPrint : $fkAddress  Amount : $amount and networktype : $networkType and status : $status at_least_created_time : ${
				formattedTime(
					at_least_created_at ?: 0
				)
			}  yesterdayStart : ${formattedTime(yesterdayStart ?: 0)}  : yesterdayEnd : ${
				formattedTime(
					yesterdayEnd ?: 0
				)
			}"
		)
		return transactionInteract.getTransactionsFlowByProvidedParametersPagingSource(
			fkAddress,
			amount,
			networkType,
			status,
			at_least_created_at,
			yesterdayStart,
			yesterdayEnd,
			tokenCode
		)
	}


	suspend fun getDistinctNetworkTypeValues() = walletInteract.getDistinctNetworkTypes()

	fun swipedRefreshClicked(onFinished: () -> Unit) {
		viewModelScope.launch {
			blockChainInteract.updateBalanceAndTransactionsPeriodically()
			greenAppInteract.requestOtherNotifItems()
			onFinished()
		}
	}

	fun initNFTInfoByHash(hash: String) {
		viewModelScope.launch {
			val nftInfo=nftInteract.getNftINFOByHash(hash)
			_nftInfoState.emit(nftInfo)
		}
	}


}
