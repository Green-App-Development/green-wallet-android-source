package com.green.wallet.data.network.dto.transaction

import com.green.wallet.data.local.entity.TransactionEntity
import com.green.wallet.presentation.tools.Status
import com.google.gson.annotations.SerializedName

/**
 * Created by bekjan on 04.07.2022.
 * email: bekjan.omirzak98@gmail.com
 */


data class SendTransResponse(
	@SerializedName("name")
	val transaction_id: String,
	@SerializedName("created_at_time")
	var created_at_time: Long,
	@SerializedName("amount")
	val amount: Double,
	@SerializedName("confirmed")
	val confirmed: Boolean,
	@SerializedName("confirmed_at_height")
	val confirmed_at_height: Long,
	@SerializedName("fee_amount")
	val fee_amount: Double
) {

	fun toTransactionEntity(
		toAddress: String,
		fkAddress: String,
		networkType: String,
		created_at_time: Long,
		amount: Double
	) = TransactionEntity(
		transaction_id,
		amount,
		created_at_time,
		confirmed_at_height,
		Status.InProgress,
		networkType,
		toAddress,
		fkAddress,
		fee_amount,
		networkType
	)


}
