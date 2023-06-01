package com.green.wallet.data.network.dto.exchange

import com.google.gson.annotations.SerializedName

data class Result(
	@SerializedName("USDT")
	val fromUSDT: FromUSDT?,
	@SerializedName("XCH")
	val fromXCH: FromXCH?
)
