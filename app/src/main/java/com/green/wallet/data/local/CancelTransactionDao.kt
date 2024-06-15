package com.green.wallet.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.green.wallet.data.local.entity.CancelTransactionEntity

@Dao
interface CancelTransactionDao {

    @Query("SELECT * FROM Canceltransactionentity WHERE addressFk=:address")
    suspend fun getCancelTransactionListByWalletAddress(address: String): List<CancelTransactionEntity>

    @Delete
    suspend fun deleteCancelTransaction(item: CancelTransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCancelTransactionEntity(cancelTransactionEntity: CancelTransactionEntity)


}