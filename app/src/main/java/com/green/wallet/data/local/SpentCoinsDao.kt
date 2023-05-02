package com.green.wallet.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.IGNORE
import androidx.room.Query
import com.green.wallet.data.local.entity.SpentCoinsEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface SpentCoinsDao {

	@Insert(onConflict = IGNORE)
	suspend fun insertSpentCoins(spentCoinsEntity: SpentCoinsEntity)

	@Query("SELECT * FROM SpentCoins WHERE fk_address=:fk_address AND code=:code")
	suspend fun getSpentCoinsByAddressCode(fk_address: String, code: String): List<SpentCoinsEntity>

	@Query("DELETE FROM SpentCoins WHERE time_created=:time_created")
	suspend fun deleteSpentConsByTimeCreated(time_created: Long): Int

	@Query("DELETE FROM SpentCoins WHERE fk_address=:fk_address")
	suspend fun deleteSpentCoinsByFkAddress(fk_address: String): Int


	@Query("SELECT * FROM SpentCoins WHERE fk_address=:fk_address AND code=:code")
	fun getSpentCoinsByAddressCodeFlow(
		fk_address: String,
		code: String
	): Flow<List<SpentCoinsEntity>>

}
