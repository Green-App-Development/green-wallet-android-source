package com.green.wallet.data.interact

import com.green.wallet.data.local.CancelTransactionDao
import com.green.wallet.data.local.entity.CancelTransactionEntity
import com.green.wallet.domain.interact.CancelTransactionInteract
import com.green.wallet.presentation.tools.VLog
import javax.inject.Inject

class CancelTransactionInteractImpl @Inject constructor(
    private val cancelTransactionDao: CancelTransactionDao
) : CancelTransactionInteract {
    override suspend fun insertCancelTransaction(cancelTransactionEntity: CancelTransactionEntity) {
        VLog.d("Inserting Cancel Transaction : $cancelTransactionEntity")
        cancelTransactionDao.insertCancelTransactionEntity(cancelTransactionEntity)
    }

}