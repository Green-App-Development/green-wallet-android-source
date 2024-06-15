package com.green.wallet.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import com.green.wallet.data.local.entity.OfferTransactionEntity
import com.green.wallet.domain.domainmodel.OfferTransaction
import com.green.wallet.presentation.tools.Status
import kotlinx.coroutines.flow.Flow
import java.util.Optional

@Dao
interface OfferTransactionDao {

    @Insert(onConflict = REPLACE)
    suspend fun saveOfferTransactionEntity(saveOfferTransactionEntity: OfferTransactionEntity): Long

    @Query(
        "SELECT * FROM OfferTransactionEntity WHERE (:fkAddress IS NULL OR addressFk=:fkAddress) " +
                "AND (:status IS NULL OR status=:status) " +
                "AND (:at_least_created_time IS NULL OR createdTime>=:at_least_created_time) " +
                "AND (:yesterday IS NULL OR (createdTime>=:yesterday " +
                "AND createdTime<=:today)) " +
                "ORDER BY createdTime DESC"
    )
    fun getAllOfferTransactionsFlowByGivenParameters(
        fkAddress: String?,
        status: Status?,
        at_least_created_time: Long?,
        yesterday: Long?,
        today: Long?,
    ): Flow<List<OfferTransactionEntity>>

    @Query("SELECT * FROM OfferTransactionEntity WHERE tranId=:tranID")
    suspend fun getOfferTransactionByTranID(tranID: String): Optional<OfferTransactionEntity>

    @Query("SELECT * FROM OfferTransactionEntity")
    suspend fun getAllOfferTransactions(): List<OfferTransactionEntity>

    @Query("SELECT * FROM OfferTransactionEntity WHERE addressFk=:addressFk AND status=:status")
    suspend fun getAllOfferTransactionsByAddressFk(
        addressFk: String,
        status: Status
    ): List<OfferTransactionEntity>

    @Query("UPDATE OfferTransactionEntity SET height=:height WHERE tranId=:tranId")
    suspend fun updateOfferTransaction(height: Int, tranId: String): Int

    @Query("UPDATE OfferTransactionEntity SET status=:status WHERE tranId=:tranId")
    suspend fun updateOfferTransactionStatus(status: Status, tranId: String): Int

    @Query("UPDATE OfferTransactionEntity SET status=:status,height=:height WHERE tranId=:tranId")
    suspend fun updateOfferTransactionStatusHeight(status: Status, height: Int, tranId: String): Int


}