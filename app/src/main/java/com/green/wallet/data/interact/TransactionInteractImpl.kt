package com.green.wallet.data.interact

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.green.wallet.data.local.TransactionDao
import com.green.wallet.data.preference.PrefsManager
import com.green.wallet.domain.domainmodel.TransferTransaction
import com.green.wallet.domain.interact.PrefsInteract
import com.green.wallet.domain.interact.TransactionInteract
import com.green.wallet.presentation.custom.getShortNetworkType
import com.green.wallet.presentation.tools.Status
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class TransactionInteractImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val prefs: PrefsInteract
) : TransactionInteract {

    override suspend fun getTransactionsByProvidedParameters(
        fkAddress: String?,
        amount: Double?,
        networkType: String?,
        status: Status?,
        at_least_created_at: Long?,
        yesterday: Long?,
        today: Long?
    ): List<TransferTransaction> {
        val timeDiff =
            prefs.getSettingLong(PrefsManager.TIME_DIFFERENCE, System.currentTimeMillis())
        val resTrans = transactionDao.getALlTransactionsByGivenParameters(
            fkAddress,
            amount,
            networkType,
            status,
            at_least_created_at,
            yesterday,
            today
        )
            .map { transaction -> transaction.toTransaction(transaction.created_at_time + timeDiff) }
        return resTrans
    }


    override fun getTransactionsFlowByProvidedParameters(
        fkAddress: String?,
        amount: Double?,
        networkType: String?,
        status: Status?,
        at_least_created_at: Long?,
        yesterday: Long?,
        today: Long?,
        tokenCode: String?
    ): Flow<List<TransferTransaction>> {
        val flowTransactionList = transactionDao.getALlTransactionsFlowByGivenParameters(
            fkAddress,
            amount,
            networkType,
            status,
            at_least_created_at,
            yesterday,
            today,
            tokenCode
        ).map { listFlow ->
            listFlow.map { tran ->
                tran.toTransaction(
                    tran.created_at_time +
                            prefs.getSettingLong(
                                PrefsManager.TIME_DIFFERENCE,
                                System.currentTimeMillis()
                            )
                )
            }
        }
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

    override fun getTransactionsFlowByProvidedParametersPagingSource(
        fkAddress: String?,
        amount: Double?,
        networkType: String?,
        status: Status?,
        at_least_created_at: Long?,
        yesterday: Long?,
        today: Long?,
        tokenCode: String?
    ): Flow<PagingData<TransferTransaction>> {
        val pagingSourceFactory = {
            transactionDao.getALlTransactionsFlowByGivenParametersPagingSource(
                fkAddress,
                amount,
                networkType,
                status,
                at_least_created_at,
                yesterday,
                today,
                tokenCode
            )
        }

        val transactionFlow = Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = pagingSourceFactory
        ).flow.map { pagingData ->
            pagingData.map { transactionEntity ->
                transactionEntity.toTransaction(
                    transactionEntity.created_at_time +
                            prefs.getSettingLong(
                                PrefsManager.TIME_DIFFERENCE,
                                System.currentTimeMillis()
                            )
                )
            }
        }
        return transactionFlow
    }

    override suspend fun deleteTransByID(tranID: String) =
        transactionDao.deleteFromTransactionByTranID(tranID)

}
