package com.green.wallet.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.IGNORE
import androidx.room.Query
import com.green.wallet.data.local.entity.TransactionEntity
import com.green.wallet.presentation.tools.Status
import kotlinx.coroutines.flow.Flow
import java.util.*


@Dao
interface TransactionDao {


	@Insert(onConflict = IGNORE)
	suspend fun insertTransaction(trans: TransactionEntity)

	@Insert(onConflict = IGNORE)
	suspend fun insertTransactions(transList: List<TransactionEntity>)

	@Query("SELECT * FROM TransactionEntity ORDER BY created_at_time DESC")
	fun getAllTransactionsFlow(): Flow<List<TransactionEntity>>

	@Query("SELECT * FROM TransactionEntity WHERE (:fkAddress IS NULL OR fkAddress=:fkAddress) AND (:networkType IS NULL OR network_type=:networkType) AND (:status IS NULL OR status=:status) AND (:qAmount IS NULL OR amount=:qAmount) AND (:at_least_created_time IS NULL OR created_at_time>=:at_least_created_time) AND (:yesterday IS NULL OR (created_at_time>=:yesterday AND created_at_time<=:today)) ORDER BY created_at_time DESC")
	suspend fun getALlTransactionsByGivenParameters(
		fkAddress: String?,
		qAmount: Double?,
		networkType: String?,
		status: Status?,
		at_least_created_time: Long?,
		yesterday: Long?,
		today: Long?
	): List<TransactionEntity>

	@Query("DELETE FROM TransactionEntity WHERE fkAddress=:address")
	suspend fun deleteTransactionsWhenWalletDeleted(address: String): Int

	@Query("SELECT * FROM TransactionEntity WHERE transaction_id=:transaction_id")
	suspend fun checkTransactionByIDExistInDB(transaction_id: String): Optional<TransactionEntity>

	@Query("SELECT * FROM TransactionEntity WHERE status=:status  ORDER BY created_at_time ASC")
	suspend fun getTransactionsByStatus(status: Status): List<TransactionEntity>

	@Query("UPDATE TransactionEntity SET status=:status,height=:height WHERE nft_coin_hash=:nft_coin_hash")
	suspend fun updateTransactionStatusHeight(status: Status, height: Long, nft_coin_hash: String)


	@Query("SELECT * FROM TransactionEntity WHERE (:fkAddress IS NULL OR fkAddress=:fkAddress) AND (:networkType IS NULL OR network_type=:networkType) AND (:status IS NULL OR status=:status) AND (:qAmount IS NULL OR amount=:qAmount) AND (:at_least_created_time IS NULL OR created_at_time>=:at_least_created_time) AND (:yesterday IS NULL OR (created_at_time>=:yesterday AND created_at_time<=:today)) AND (:tokenCode IS NULL OR code LIKE '%' || :tokenCode || '%') ORDER BY created_at_time DESC")
	fun getALlTransactionsFlowByGivenParameters(
		fkAddress: String?,
		qAmount: Double?,
		networkType: String?,
		status: Status?,
		at_least_created_time: Long?,
		yesterday: Long?,
		today: Long?,
		tokenCode: String?
	): Flow<List<TransactionEntity>>

	@Query("SELECT * FROM TransactionEntity WHERE status=:status AND fkAddress=:address AND code=:code")
	fun getMempoolTransactionsByAddressAndToken(
		status: Status,
		address: String,
		code: String
	): List<TransactionEntity>

	@Query("SELECT * FROM TransactionEntity WHERE fkAddress=:fkAddress AND status=:status")
	suspend fun checkInProgressTransactionsByAddress(
		fkAddress: String,
		status: Status = Status.InProgress
	): Optional<TransactionEntity>

	@Query("SELECT * FROM TransactionEntity WHERE fkAddress=:fkAddress AND status=:status AND amount=:amount AND height=:height AND to_dest_hash=:dest_hash AND code=:tokenCode")
	suspend fun checkoutOutGoingTransactionsByAddressHeightAmountDestHash(
		fkAddress: String,
		amount: Double,
		height: Long,
		dest_hash: String,
		tokenCode: String,
		status: Status = Status.Outgoing
	): Optional<TransactionEntity>

	@Query("SELECT * FROM TransactionEntity WHERE fkAddress=:fkAddress AND STATUS=:status AND amount=:amount AND height=:height")
	suspend fun checkoutOutgoingTransactionsByAddressHeightSecondCase(
		fkAddress: String,
		status: Status = Status.Outgoing,
		amount: Double,
		height: Long
	): Optional<TransactionEntity>


	@Query("SELECT * FROM TransactionEntity WHERE (:fkAddress IS NULL OR fkAddress=:fkAddress) AND (:networkType IS NULL OR network_type=:networkType) AND (:status IS NULL OR status=:status) AND (:qAmount IS NULL OR amount=:qAmount) AND (:at_least_created_time IS NULL OR created_at_time>=:at_least_created_time) AND (:yesterday IS NULL OR (created_at_time>=:yesterday AND created_at_time<=:today)) AND (:tokenCode IS NULL OR code LIKE '%' || :tokenCode || '%') ORDER BY created_at_time DESC")
	fun getALlTransactionsFlowByGivenParametersPagingSource(
		fkAddress: String?,
		qAmount: Double?,
		networkType: String?,
		status: Status?,
		at_least_created_time: Long?,
		yesterday: Long?,
		today: Long?,
		tokenCode: String?
	): PagingSource<Int, TransactionEntity>


}
