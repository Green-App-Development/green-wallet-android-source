package com.android.greenapp.data.local

import androidx.room.*
import com.android.greenapp.data.local.entity.TokenEntity
import java.util.*

/**
 * Created by bekjan on 12.07.2022.
 * email: bekjan.omirzak98@gmail.com
 */

@Dao
interface TokenDao {


	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertToken(tokenEntity: TokenEntity)

	@Query("UPDATE TokenEntity SET price=:price WHERE code=:code")
	suspend fun updateTokenPrice(price: Double, code: String): Int

	@Query("SELECT * FROM TokenEntity WHERE  (:nameCode IS NULL OR name LIKE '%' ||  :nameCode || '%') OR  (:nameCode IS NULL OR code LIKE '%' || :nameCode || '%') AND enabled=:enabled")
	suspend fun getTokenListAndSearch(nameCode: String?, enabled: Boolean = true): List<TokenEntity>

	@Query("SELECT * FROM TokenEntity WHERE hash LIKE '%' || :hash  || '%'")
	suspend fun getTokenByHash(hash: String): Optional<TokenEntity>

	@Query("SELECT * FROM TokenEntity WHERE code=:code")
	suspend fun getTokenByCode(code: String): Optional<TokenEntity>

	@Query("SELECT * FROM TokenEntity WHERE default_tail==1")
	suspend fun getTokensDefaultOnScreen(): List<TokenEntity>


}
