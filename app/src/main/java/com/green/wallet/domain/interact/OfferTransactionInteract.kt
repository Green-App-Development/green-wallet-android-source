package com.green.wallet.domain.interact

import com.green.wallet.domain.domainmodel.OfferTransaction
import com.green.wallet.presentation.tools.Status
import kotlinx.coroutines.flow.Flow

interface OfferTransactionInteract {

    suspend fun saveOfferTransaction(addressFk: String, offer: OfferTransaction)

    fun getOfferTransactionListFlow(
        fkAddress: String?,
        amount: Double?,
        status: Status?,
        at_least_created_at: Long?,
        yesterday: Long?,
        today: Long?,
    ): Flow<List<OfferTransaction>>

}