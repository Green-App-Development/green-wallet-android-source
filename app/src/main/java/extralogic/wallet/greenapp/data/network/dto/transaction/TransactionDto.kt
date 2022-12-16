package extralogic.wallet.greenapp.data.network.dto.transaction

import com.google.gson.annotations.SerializedName

data class TransactionDto(
	@SerializedName("name")
	val transaction_id: String,
	@SerializedName("created_at_time")
	var created_at_time: Long,
	@SerializedName("amount")
	var amount: Double,
	@SerializedName("confirmed")
	val confirmed: Boolean,
	@SerializedName("confirmed_at_height")
	val confirmed_at_height: Long,
	@SerializedName("type")
	val type: Int?,
	@SerializedName("to_address")
	val to_address: String,
	@SerializedName("fee_amount")
	val fee_amount: Double
)
