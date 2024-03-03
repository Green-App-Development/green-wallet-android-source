package com.green.wallet.data.local

import androidx.room.*
import com.green.wallet.data.local.entity.NFTInfoEntity
import java.util.*


@Dao
interface NftInfoDao {


	@Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
	suspend fun insertNftInfoEntity(nftInfoEntity: NFTInfoEntity)

	@Query("SELECT * FROM NFTInfoEntity WHERE nft_coin_hash=:nft_coin_hash")
	suspend fun getNftInfoEntityByNftCoinHash(nft_coin_hash: String): Optional<NFTInfoEntity>

	@Query("DELETE FROM NFTInfoEntity WHERE nft_coin_hash=:nft_coin_hash")
	suspend fun deleteNFTInfoById(nft_coin_hash: String): Int

	@Query("UPDATE NFTInfoEntity SET spent=:spent WHERE nft_coin_hash=:nft_coin_hash")
	suspend fun updateSpentNFTInfoByNFTCoinHash(spent: Boolean, nft_coin_hash: String): Int

	@Query("UPDATE NFTInfoEntity SET isPending=:isPending WHERE nft_coin_hash=:nft_coin_hash")
	suspend fun updateIsPendingNFTInfoByNFTCoinHash(isPending: Boolean, nft_coin_hash: String): Int

}
