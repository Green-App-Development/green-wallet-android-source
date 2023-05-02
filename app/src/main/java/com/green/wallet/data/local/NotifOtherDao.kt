package com.green.wallet.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.IGNORE
import androidx.room.Query
import com.green.wallet.data.local.entity.NotifOtherEntity
import java.util.*


@Dao
interface NotifOtherDao {

    @Query("SELECT * FROM NotificationOther WHERE   (:at_least_created_time IS NULL OR created_at_time>=:at_least_created_time) AND (:yesterday IS NULL OR (created_at_time>=:yesterday AND created_at_time<=:today)) ORDER BY created_at_time DESC")
    suspend fun getALlNotifOthersByGivenParameters(
        at_least_created_time: Long?,
        yesterday: Long?,
        today: Long?
    ): List<NotifOtherEntity>

    @Insert(onConflict = IGNORE)
    suspend fun insertingNotifOther(notifOther:NotifOtherEntity)

    @Insert(onConflict = IGNORE)
    suspend fun insertOtherNotifItems(otherNotifItems: List<NotifOtherEntity>)

    @Query("SELECT * FROM Notificationother WHERE guid=:guid")
    suspend fun getNotifOtherItemByGuid(guid: String): Optional<NotifOtherEntity>


}
