package com.green.wallet.data.network

import com.google.gson.annotations.SerializedName

data class BaseResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("result")
    val result: Any?,
    @SerializedName("error")
    val error: String?,
    @SerializedName("status")
    var status:String?,
	@SerializedName("error_code")
	var error_code:Int?
)
