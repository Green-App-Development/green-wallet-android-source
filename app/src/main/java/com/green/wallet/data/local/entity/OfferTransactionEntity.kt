package com.green.wallet.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.green.wallet.domain.domainmodel.OfferTransaction
import com.green.wallet.presentation.main.dapp.trade.models.Token
import com.green.wallet.presentation.tools.Status


@Entity(
    tableName = "OfferTransactionEntity",
    foreignKeys = [
        ForeignKey(
            entity = WalletEntity::class,
            parentColumns = ["address"],
            childColumns = ["addressFk"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class OfferTransactionEntity(
    @PrimaryKey
    val tranId: String,
    @ColumnInfo(name = "status")
    val status: Status,
    val addressFk: String,
    val requested: List<Token>,
    val offered: List<Token>,
    val acceptOffer: Boolean = false,
    val createdTime: Long,
    val source: String = "",
    val hashTransaction: String = " ",
    val fee: Double = 0.0,
    val height: Long = 0L,
) {

    fun toOfferTransaction() = OfferTransaction(
        tranId,
        createdTime,
        requested,
        offered,
        source,
        hashTransaction,
        fee,
        height,
        acceptOffer
    )

}