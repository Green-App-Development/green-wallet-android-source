package com.android.greenapp.data.network.dto.blockchain

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName


data class PublicKeyDto(
    @SerializedName("fingerprint")
    val fingerprint: Long,
    @SerializedName("success")
    val success: Boolean
)