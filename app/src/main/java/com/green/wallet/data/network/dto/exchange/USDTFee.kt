package com.green.wallet.data.network.dto.exchange

import com.google.gson.annotations.SerializedName

data class USDTFee(
	@SerializedName("USDT")
	val usdt: String,
	@SerializedName("XCH")
	val xch: String,
	@SerializedName("exchange")
	val exchange: String
)
