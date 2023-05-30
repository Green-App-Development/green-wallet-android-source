package com.green.wallet.data.network.dto.exchange

import com.google.gson.annotations.SerializedName

data class FromXCH(
	@SerializedName("USDT")
	val toUSDT: ToUSDT,
	val address: String
)
