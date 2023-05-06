package com.green.wallet.data.network.dto.transaction

import com.green.wallet.data.local.entity.TransactionEntity
import com.green.wallet.presentation.tools.Status
import com.google.gson.annotations.SerializedName

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



}
