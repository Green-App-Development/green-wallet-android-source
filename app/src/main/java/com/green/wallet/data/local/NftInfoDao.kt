package com.green.wallet.data.local

import androidx.room.Dao
import androidx.room.Ignore
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.green.wallet.data.local.entity.NFTInfoEntity


@Dao
interface NftInfoDao {


	@Insert(onConflict = OnConflictStrategy.IGNORE)
	suspend fun insertNftInfoEntity(nftInfoEntity: NFTInfoEntity)


}
