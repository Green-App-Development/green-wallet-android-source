package com.green.wallet.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey
import com.green.wallet.domain.domainmodel.NFTCoin


@Entity(
    tableName = "NFTCoinEntity",
    foreignKeys = [
        ForeignKey(
            entity = WalletEntity::class,
            parentColumns = ["address"],
            childColumns = ["address_fk"],
            onDelete = CASCADE
        )
    ]
)
data class NFTCoinEntity(
    @PrimaryKey(autoGenerate = false)
    val coin_info: String,
    val address_fk: String,
    val coin_hash: String,
    val amount: Int,
    val confirmed_block_index: Long,
    val spent_block_index: Long,
    val time_stamp: Long,
    val puzzle_hash: String
) {
    fun toNftCoin() = NFTCoin(
        coin_info,
        address_fk,
        coin_hash,
        amount,
        confirmed_block_index,
        spent_block_index,
        time_stamp,
        puzzle_hash
    )
}
