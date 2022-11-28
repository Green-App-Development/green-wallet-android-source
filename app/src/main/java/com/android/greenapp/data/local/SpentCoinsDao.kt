package com.android.greenapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.android.greenapp.data.local.entity.SpentCoinsEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface SpentCoinsDao {

	@Insert(onConflict = REPLACE)
	suspend fun insertSpentCoins(spentCoinsEntity: SpentCoinsEntity)

	@Query("SELECT * FROM SpentCoins WHERE fk_address=:fk_address AND code=:code")
	suspend fun getSpentCoinsByAddressCode(fk_address: String, code: String): List<SpentCoinsEntity>

	@Query("DELETE FROM SpentCoins WHERE time_created=:time_created")
	suspend fun deleteSpentConsByTimeCreated(time_created: Long): Int


	@Query("SELECT * FROM SpentCoins WHERE fk_address=:fk_address AND code=:code")
	fun getSpentCoinsByAddressCodeFlow(
		fk_address: String,
		code: String
	): Flow<List<SpentCoinsEntity>>

}
