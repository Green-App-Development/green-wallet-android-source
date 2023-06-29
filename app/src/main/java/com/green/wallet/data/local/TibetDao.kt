package com.green.wallet.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import com.green.wallet.data.local.entity.TibetSwapEntity


@Dao
interface TibetDao {

	@Insert(onConflict = REPLACE)
	suspend fun insertTibetEntity(tibetSwapEntity: TibetSwapEntity)

}
