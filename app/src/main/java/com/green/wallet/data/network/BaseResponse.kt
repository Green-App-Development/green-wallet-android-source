package com.green.wallet.data.network

import com.green.wallet.data.network.dto.transaction.SendTransResponse
import com.google.gson.annotations.SerializedName

data class BaseResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("result")
    val result: Any?,
    @SerializedName("transaction")
    val sendTransResponse: SendTransResponse?,
    @SerializedName("error")
    val error: String?,
    @SerializedName("status")
    var status:String?,
	@SerializedName("error_code")
	var error_code:Int?
)
