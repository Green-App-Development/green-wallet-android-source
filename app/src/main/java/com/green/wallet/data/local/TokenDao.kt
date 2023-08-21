package com.green.wallet.data.local

import androidx.room.*
import com.green.wallet.data.local.entity.TokenEntity
import java.util.*


@Dao
interface TokenDao {


	@Insert(onConflict = OnConflictStrategy.IGNORE)
	suspend fun insertToken(tokenEntity: TokenEntity)

	@Query("UPDATE TokenEntity SET price=:price WHERE code=:code")
	suspend fun updateTokenPrice(price: Double, code: String): Int

	@Query("SELECT * FROM TokenEntity WHERE  ((:nameCode IS NULL OR name LIKE '%' ||  :nameCode || '%') OR  (:nameCode IS NULL OR code LIKE '%' || :nameCode || '%')) AND enabled=:enabled")
	suspend fun getTokenListAndSearch(nameCode: String?, enabled: Boolean = true): List<TokenEntity>

	@Query("SELECT * FROM TokenEntity WHERE hash LIKE '%' || :hash  || '%'")
	suspend fun getTokenByHash(hash: String): Optional<TokenEntity>

	@Query("SELECT * FROM TokenEntity WHERE code=:code")
	suspend fun getTokenByCode(code: String): Optional<TokenEntity>

	@Query("SELECT * FROM TokenEntity WHERE default_tail==1")
	suspend fun getTokensDefaultOnScreen(): List<TokenEntity>

	@Query("UPDATE TokenEntity SET enabled=:enabled WHERE code=:code")
	suspend fun updateTokenEnable(code: String, enabled: Boolean = false): Int

	@Query("UPDATE TokenEntity SET pair_id=:pair_id WHERE hash=:hash")
	suspend fun updateTokenEntityPairIDByHash(pair_id: String, hash: String): Int

	@Query("SELECT * FROM TokenEntity WHERE pair_id!=''")
	suspend fun getTokenListEntityPairIdIsNotEmpty(): List<TokenEntity>

	@Query("SELECT * FROM TokenEntity WHERE code LIKE '%' || :space || '%'")
	suspend fun getTibetTokenList(space: String = "-"): List<TokenEntity>

}
