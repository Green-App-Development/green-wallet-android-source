package com.green.wallet.data.network.dto.exchange

import com.google.gson.annotations.SerializedName

data class FromUSDT(
	@SerializedName("XCH")
    val toXCH: ToXCH,
	val address: String
)
