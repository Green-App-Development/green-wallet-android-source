package com.android.greenapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.IGNORE
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Update
import com.android.greenapp.data.local.entity.TransactionEntity
import com.android.greenapp.presentation.tools.Status
import kotlinx.coroutines.flow.Flow
import java.util.*


@Dao
interface TransactionDao {


    @Insert(onConflict = IGNORE)
    suspend fun insertTransaction(trans: TransactionEntity)

    @Insert(onConflict = REPLACE)
    suspend fun insertTransactions(transList: List<TransactionEntity>)

    @Query("SELECT * FROM TransactionEntity ORDER BY created_at_time DESC")
    fun getAllTransactionsFlow(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM TransactionEntity WHERE (:fingerPrint IS NULL OR fkFingerPrint=:fingerPrint) AND (:networkType IS NULL OR network_type=:networkType) AND (:status IS NULL OR status=:status) AND (:qAmount IS NULL OR amount=:qAmount) AND (:at_least_created_time IS NULL OR created_at_time>=:at_least_created_time) AND (:yesterday IS NULL OR (created_at_time>=:yesterday AND created_at_time<=:today)) ORDER BY created_at_time DESC")
    suspend fun getALlTransactionsByGivenParameters(
        fingerPrint: Long?,
        qAmount: Double?,
        networkType: String?,
        status: Status?,
        at_least_created_time: Long?,
        yesterday: Long?,
        today: Long?
    ): List<TransactionEntity>

    @Query("DELETE FROM TransactionEntity WHERE fkFingerPrint=:fingerPrint")
    suspend fun deleteTransactionsWhenWalletDeleted(fingerPrint: Long): Int

    @Query("SELECT * FROM TransactionEntity WHERE transaction_id=:transaction_id")
    suspend fun checkTransactionByIDExistInDB(transaction_id: String): Optional<TransactionEntity>

    @Query("SELECT * FROM TransactionEntity WHERE status=:status  ORDER BY created_at_time ASC")
	suspend fun getTransactionsByStatus(status: Status):List<TransactionEntity>

	@Query("UPDATE TransactionEntity SET status=:status,height=:height WHERE transaction_id=:transaction_id")
	suspend fun updateTransactionStatusHeight(status: Status,height:Long,transaction_id: String)


	@Query("SELECT * FROM TransactionEntity WHERE (:fingerPrint IS NULL OR fkFingerPrint=:fingerPrint) AND (:networkType IS NULL OR network_type=:networkType) AND (:status IS NULL OR status=:status) AND (:qAmount IS NULL OR amount=:qAmount) AND (:at_least_created_time IS NULL OR created_at_time>=:at_least_created_time) AND (:yesterday IS NULL OR (created_at_time>=:yesterday AND created_at_time<=:today)) ORDER BY created_at_time DESC")
	fun getALlTransactionsFlowByGivenParameters(
		fingerPrint: Long?,
		qAmount: Double?,
		networkType: String?,
		status: Status?,
		at_least_created_time: Long?,
		yesterday: Long?,
		today: Long?
	): Flow<List<TransactionEntity>>



}
