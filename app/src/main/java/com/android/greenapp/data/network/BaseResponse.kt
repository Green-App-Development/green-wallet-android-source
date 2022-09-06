package com.android.greenapp.data.network

import com.android.greenapp.data.network.dto.blockchain.PrivateKeyDto
import com.android.greenapp.data.network.dto.transaction.SendTransResponse
import com.android.greenapp.data.network.dto.transaction.TransactionDto
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
    @SerializedName("private_key")
    val private_key: PrivateKeyDto?,
    @SerializedName("public_key_fingerprints")
    val public_key_fingerprints: List<Long>?,
    @SerializedName("wallet_balance")
    val wallet_balance: Any?,
    @SerializedName("transactions")
    val transactions: List<TransactionDto>?,
    @SerializedName("transaction")
    val sendTransResponse: SendTransResponse?,
    @SerializedName("error")
    val error: String?,
    @SerializedName("status")
    var status:String?
)