package com.green.wallet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class CancelTransaction(
    @PrimaryKey
    val offerTranID: String,
    val timeCreated: Long
)