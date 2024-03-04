package com.green.wallet.data.interact

import com.green.wallet.data.local.OfferTransactionDao
import com.green.wallet.domain.domainmodel.OfferTransaction
import com.green.wallet.domain.interact.OfferTransactionInteract
import com.green.wallet.presentation.tools.Status
import com.green.wallet.presentation.tools.VLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OfferTransactionInteractImpl @Inject constructor(
    private val offerTranDao: OfferTransactionDao
) : OfferTransactionInteract {

    override suspend fun saveOfferTransaction(addressFk: String, offer: OfferTransaction) {
        val offerTranEntity = offer.toOfferTransactionEntity(addressFk)
        VLog.d("Inserting offer Transaction : $offerTranEntity")
        offerTranDao.saveOfferTransactionEntity(offerTranEntity)
    }

    override fun getOfferTransactionListFlow(
        fkAddress: String?,
        amount: Double?,
        status: Status?,
        at_least_created_at: Long?,
        yesterday: Long?,
        today: Long?,
    ): Flow<List<OfferTransaction>> {
        return offerTranDao.getAllOfferTransactionsFlowByGivenParameters(
            fkAddress = fkAddress,
            status = status,
            at_least_created_time = at_least_created_at,
            yesterday = yesterday,
            today = today
        ).map { it -> it.map { it.toOfferTransaction() } }
    }

    override suspend fun getOfferTransactionByTranID(tranID: String): OfferTransaction? {
        val result = offerTranDao.getOfferTransactionByTranID(tranID)
        if (result.isPresent)
            return result.get().toOfferTransaction()
        return null
    }

    override suspend fun getAllOfferTransactions(): List<OfferTransaction> {
        return offerTranDao.getAllOfferTransactions().map { it.toOfferTransaction() }
    }

}