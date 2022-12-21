package com.green.wallet.data.network

import com.green.wallet.data.network.dto.transaction.SendTransResponse
import com.google.gson.annotations.SerializedName

/**
 * Created by bekjan on 25.05.2022.
 * email: bekjan.omirzak98@gmail.com
 */
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
