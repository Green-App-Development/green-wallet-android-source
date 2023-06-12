package com.green.wallet.data.network.dto.exchange

import com.google.gson.annotations.SerializedName

data class ToUSDT(
	val address: String,
	val max: String,
	val min: String,
	val rate: String,
	@SerializedName("fee")
	val xchFee: XCHFee
)
