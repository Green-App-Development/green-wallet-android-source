package com.android.greenapp.domain.entity

import com.android.greenapp.data.local.entity.TransactionEntity
import com.android.greenapp.presentation.tools.Status


data class Transaction(
	val transaction_id: String,
	var amount: Double,
	val created_at_time: Long,
	val confirmed_at_height: Long,
	val status: Status,
	val networkType: String,
	val to_dest_hash: String,
	val fkAddress: String,
	val fee_amount: Double,
	var code:String
) {
    fun toTransactionEntity() =
        TransactionEntity(
            transaction_id,
            amount,
            created_at_time,
            confirmed_at_height,
            status,
            networkType,
            to_dest_hash,
			fkAddress,
            fee_amount,
			code
        )
}
