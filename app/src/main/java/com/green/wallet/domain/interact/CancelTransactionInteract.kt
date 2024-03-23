package com.green.wallet.domain.interact

import com.green.wallet.data.local.entity.CancelTransactionEntity

interface CancelTransactionInteract {

    suspend fun insertCancelTransaction(cancelTransactionEntity: CancelTransactionEntity)

}