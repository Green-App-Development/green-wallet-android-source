package com.green.wallet.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.green.wallet.data.local.entity.NFTCoinEntity
import java.util.*


@Dao
interface NftCoinsDao {

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertNftCoinsEntity(nftCoinEntity: NFTCoinEntity)

	@Query("SELECT * FROM NFTCoinEntity WHERE coin_info=:nft_hash")
	suspend fun getNFTCoinByParentCoinInfo(nft_hash: String): Optional<NFTCoinEntity>

	@Query("DELETE FROM NFTCoinEntity WHERE coin_info=:nft_hash")
	suspend fun deleteNFTCoinEntityByCoinInfo(nft_hash: String):Int

}
