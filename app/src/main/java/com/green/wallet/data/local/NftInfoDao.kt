package com.green.wallet.data.local

import androidx.room.*
import com.green.wallet.data.local.entity.NFTInfoEntity
import java.util.*


@Dao
interface NftInfoDao {


	@Insert(onConflict = OnConflictStrategy.IGNORE)
	suspend fun insertNftInfoEntity(nftInfoEntity: NFTInfoEntity)

	@Query("SELECT * FROM NFTInfoEntity WHERE nft_coin_hash=:nft_coin_hash")
	suspend fun getNftInfoEntityByNftCoinHash(nft_coin_hash: String): Optional<NFTInfoEntity>

}