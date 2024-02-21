package com.green.wallet.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.green.wallet.domain.domainmodel.TransferTransaction
import com.green.wallet.presentation.tools.Status


@Entity(
	tableName = "TransactionEntity",
//	foreignKeys = [
//		ForeignKey(
//			entity = WalletEntity::class,
//			parentColumns = ["address"],
//			childColumns = ["fkAddress"],
//			onDelete = ForeignKey.CASCADE
//		)
//	]
)
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
	val fee_amount: Double,
	@ColumnInfo(name = "code")
	val code: String,
	@ColumnInfo(name = "confirm_height", defaultValue = "0")
	val confirm_height: Int = 0,
	@ColumnInfo(name = "nft_coin_hash", defaultValue = "")
	val nft_coin_hash: String = ""
) {

	fun toTransaction(localTime: Long) =
        TransferTransaction(
            transaction_id,
            amount,
            localTime,
            height,
            status,
            networkType,
            to_dest_hash,
            fkAddress,
            fee_amount,
            code,
			nft_coin_hash
		)

}
