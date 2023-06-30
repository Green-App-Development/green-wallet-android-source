package com.green.wallet.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Update
import com.green.wallet.data.local.entity.TibetSwapEntity
import com.green.wallet.domain.domainmodel.OrderItem
import com.green.wallet.presentation.tools.OrderStatus
import kotlinx.coroutines.flow.Flow
import java.util.Optional


@Dao
interface TibetDao {

	@Insert(onConflict = REPLACE)
	suspend fun insertTibetEntity(tibetSwapEntity: TibetSwapEntity)

	@Query("SELECT * FROM TibetSwapEntity WHERE status=:status")
	suspend fun getTibetSwapListInProgressStatus(status: OrderStatus): List<TibetSwapEntity>

	@Query("UPDATE TibetSwapEntity SET status=:status WHERE offer_id=:offer_id")
	suspend fun updateTibetSwapEntityStatusToCompleted(status: OrderStatus, offer_id: String)

	@Query("UPDATE TibetSwapEntity SET height=:height WHERE offer_id=:offer_id")
	suspend fun updateTibetSwapEntityHeightToCompleted(height: Int, offer_id: String)


	@Query("SELECT * FROM TibetSwapEntity ORDER BY time_created DESC")
	fun getTibetSwapEntitiesListFlow(): Flow<List<TibetSwapEntity>>

	@Query("SELECT * FROM TibetSwapEntity WHERE offer_id=:offer_id")
	fun getTibetSwapEntityByOfferId(offer_id: String): Flow<TibetSwapEntity>


}
