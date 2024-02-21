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
import com.green.wallet.domain.interact.TransactionInteract
import com.green.wallet.domain.interact.WalletInteract
import com.green.wallet.presentation.tools.Status
import com.green.wallet.presentation.tools.VLog
import com.greenwallet.core.base.BaseIntentViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject


class TransactionsViewModel @Inject constructor(
    private val transactionInteract: TransactionInteract,
    private val blockChainInteract: BlockChainInteract,
    private val walletInteract: WalletInteract,
    private val greenAppInteract: GreenAppInteract,
    private val nftInteract: NFTInteract,
    private val dAppOfferInteract: DAppOfferInteract
) : BaseIntentViewModel<TransactionState, TransactionEvent, TransactionIntent>(TransactionState()) {

    private val _nftInfoState = MutableStateFlow<NFTInfo?>(null)
    val nftInfoState = _nftInfoState.asStateFlow()

    init {
//        viewModelScope.launch {
//            delay(2000L)
//            setEvent(TransactionEvent.BottomSheetCAT(null))
//        }
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


    fun getAllQueriedFlowTransactionList(
        fkAddress: String?,
        amount: Double?,
        networkType: String?,
        status: Status?,
        at_least_created_at: Long?,
        yesterdayStart: Long?,
        yesterdayEnd: Long?,
        tokenCode: String?
    ): Flow<List<TransferTransaction>> {
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
        }
    }


}
