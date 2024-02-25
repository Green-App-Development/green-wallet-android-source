package com.green.wallet.presentation.main.transaction

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.map
import com.example.common.tools.formattedTime
import com.green.wallet.domain.domainmodel.NFTInfo
import com.green.wallet.domain.domainmodel.Transaction
import com.green.wallet.domain.domainmodel.TransferTransaction
import com.green.wallet.domain.interact.BlockChainInteract
import com.green.wallet.domain.interact.DAppOfferInteract
import com.green.wallet.domain.interact.GreenAppInteract
import com.green.wallet.domain.interact.NFTInteract
import com.green.wallet.domain.interact.OfferTransactionInteract
import com.green.wallet.domain.interact.TransactionInteract
import com.green.wallet.domain.interact.WalletInteract
import com.green.wallet.presentation.tools.Status
import com.green.wallet.presentation.tools.VLog
import com.greenwallet.core.base.BaseIntentViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


class TransactionsViewModel @Inject constructor(
    private val transactionInteract: TransactionInteract,
    private val blockChainInteract: BlockChainInteract,
    private val walletInteract: WalletInteract,
    private val greenAppInteract: GreenAppInteract,
    private val nftInteract: NFTInteract,
    private val dAppOfferInteract: DAppOfferInteract,
    private val offerTransactionInteract: OfferTransactionInteract
) : BaseIntentViewModel<TransactionState, TransactionEvent, TransactionIntent>(TransactionState()) {

    private val _nftInfoState = MutableStateFlow<NFTInfo?>(null)
    val nftInfoState = _nftInfoState.asStateFlow()

    init {

    }


    suspend fun getAllQueriedTransactionList(
        fkAddress: String?,
        amount: Double?,
        networkType: String?,
        status: Status?,
        at_least_created_at: Long?,
        yesterdayStart: Long?,
        yesterdayEnd: Long?
    ): List<TransferTransaction> {
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

    private var transactionJob: Job? = null

    fun getAllQueriedFlowTransactionList(
        fkAddress: String?,
        amount: Double?,
        networkType: String?,
        status: Status?,
        at_least_created_at: Long?,
        yesterdayStart: Long?,
        yesterdayEnd: Long?,
        tokenCode: String?
    ) {
        transactionJob?.cancel()
        transactionJob = viewModelScope.launch {

            val flowOffer = offerTransactionInteract.getOfferTransactionListFlow(
                fkAddress = fkAddress,
                status = status,
                at_least_created_at = at_least_created_at,
                yesterday = yesterdayStart,
                today = yesterdayEnd,
                amount = null
            )

            val flowTransfer = transactionInteract.getTransactionsFlowByProvidedParameters(
                fkAddress,
                amount,
                networkType,
                status,
                at_least_created_at,
                yesterdayStart,
                yesterdayEnd,
                tokenCode
            )

            flowOffer.combine(flowTransfer) { offer, transfer ->
                _viewState.update { it.copy(transactionList = offer + transfer) }
            }.collect()
        }
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
//        VLog.d(
//            "FingerPrint : $fkAddress  Amount : $amount and networktype : $networkType and status : $status at_least_created_time : ${
//                formattedTime(
//                    at_least_created_at ?: 0
//                )
//            }  yesterdayStart : ${formattedTime(yesterdayStart ?: 0)}  : yesterdayEnd : ${
//                formattedTime(
//                    yesterdayEnd ?: 0
//                )
//            }"
//        )

        return transactionInteract.getTransactionsFlowByProvidedParametersPagingSource(
            fkAddress,
            amount,
            networkType,
            status,
            at_least_created_at,
            yesterdayStart,
            yesterdayEnd,
            tokenCode
        ).map { it ->
            it.map { it }
        }
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
            val nftInfo = nftInteract.getNftINFOByHash(hash)
            _nftInfoState.emit(nftInfo)
        }
    }

    override fun handleIntent(intent: TransactionIntent) {
        when (intent) {
            is TransactionIntent.OnSpeedyTran -> {
                setEvent(TransactionEvent.SpeedyBtmDialog(intent.tran))
            }

            is TransactionIntent.OnDeleteTransaction -> {
                setEvent(TransactionEvent.ShowWarningDeletionDialog(intent.tran))
            }

            is TransactionIntent.DeleteTransaction -> {
                viewModelScope.launch {
                    transactionInteract.deleteTransByID(intent.tran.transactionId)
                }
            }

            is TransactionIntent.OnShowTransactionDetails -> {
                setEvent(TransactionEvent.ShowTransactionDetails(intent.tran))
            }
        }
    }


}
