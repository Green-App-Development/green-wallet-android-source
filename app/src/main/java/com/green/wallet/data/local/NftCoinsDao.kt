package com.green.wallet.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.green.wallet.data.local.entity.NFTCoinEntity


@Dao
interface NftCoinsDao {

	@Insert(onConflict = OnConflictStrategy.IGNORE)
	suspend fun insertNftCoinsEntity(nftCoinEntity: NFTCoinEntity)


}
