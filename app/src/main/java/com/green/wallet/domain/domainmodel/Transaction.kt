package com.green.wallet.domain.domainmodel

import com.green.wallet.data.local.entity.TransactionEntity
import com.green.wallet.presentation.tools.Status


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
)
