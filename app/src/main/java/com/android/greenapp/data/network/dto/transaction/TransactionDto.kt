package com.android.greenapp.data.network.dto.transaction

import com.android.greenapp.data.local.entity.TransactionEntity
import com.android.greenapp.domain.entity.Transaction
import com.android.greenapp.presentation.tools.Status
import com.google.gson.annotations.SerializedName

data class TransactionDto(
    @SerializedName("name")
    val transaction_id: String,
    @SerializedName("created_at_time")
    var created_at_time: Long,
    @SerializedName("amount")
    val amount: Double,
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
) {

    fun toTransaction(fingerPrint: Long, status: Status, networkType: String) = Transaction(
        transaction_id,
        amount,
        created_at_time,
        confirmed_at_height,
        status,
        networkType,
        to_address,
        fingerPrint,
        fee_amount
    )

    fun toTransactionEntity(
        fingerPrint: Long,
        status: Status,
        networkType: String,
        amount: Double,
        created_at_time: Long
    ) = TransactionEntity(
        transaction_id,
        amount,
        created_at_time,
        confirmed_at_height,
        status,
        networkType,
        to_address,
        fingerPrint,
        fee_amount
    )


}