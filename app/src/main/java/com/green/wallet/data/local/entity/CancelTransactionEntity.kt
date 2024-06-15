package com.green.wallet.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(
    tableName = "CancelTransactionEntity",
    foreignKeys = [
        ForeignKey(
            entity = WalletEntity::class,
            parentColumns = ["address"],
            childColumns = ["addressFk"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CancelTransactionEntity(
    @PrimaryKey
    val offerTranID: String,
    val addressFk:String,
    val createAtTime: Long,
)