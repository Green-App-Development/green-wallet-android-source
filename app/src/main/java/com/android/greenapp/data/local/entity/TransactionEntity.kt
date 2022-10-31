package com.android.greenapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.greenapp.domain.entity.Transaction
import com.android.greenapp.presentation.tools.Status


@Entity(tableName = "TransactionEntity")
data class TransactionEntity(
	@ColumnInfo(name = "transaction_id")
    @PrimaryKey(autoGenerate = false)
    var transaction_id: String,
	@ColumnInfo(name = "amount")
    var amount: Double,
	@ColumnInfo(name = "created_at_time")
    val created_at_time: Long,
	@ColumnInfo(name = "height")
    val height: Long,
	@ColumnInfo(name = "status")
    val status: Status,
	@ColumnInfo(name = "network_type")
    val networkType: String,
	@ColumnInfo(name = "to_dest_hash")
    val to_dest_hash: String,
	@ColumnInfo(name = "fkAddress")
    val fkAddress: String,
	@ColumnInfo(name = "fee_amount")
    val fee_amount:Double,
	@ColumnInfo(name="code")
	val code:String
) {

    fun toTransaction() =
        Transaction(transaction_id, amount, created_at_time, height, status, networkType, to_dest_hash,fkAddress,fee_amount,code)

}
