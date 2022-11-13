package com.android.greenapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.greenapp.domain.domainmodel.SpentCoin


@Entity(tableName = "SpentCoins")
data class SpentCoinsEntity(
    @PrimaryKey(autoGenerate = false)
    val parent_coin_info: String,
    val puzzle_hash: String,
    val amount: Double,
    val fk_address: String,
    val code: String,
    val time_created:Long
) {

    fun toSpentCoin(amount: Long) = SpentCoin(parent_coin_info, puzzle_hash, amount)

}