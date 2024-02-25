package com.green.wallet.domain.interact

import com.green.wallet.domain.domainmodel.OfferTransaction

interface OfferTransactionInteract {

    suspend fun saveOfferTransaction(offer: OfferTransaction)

}