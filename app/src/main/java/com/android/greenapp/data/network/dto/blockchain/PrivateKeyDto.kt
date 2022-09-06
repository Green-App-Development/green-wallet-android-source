package com.android.greenapp.data.network.dto.blockchain

import com.google.gson.annotations.SerializedName


data class PrivateKeyDto(
    @SerializedName("fingerprint")
    val fingerprint: Long,
    @SerializedName("pk")
    val pk: String,
    @SerializedName("seed")
    val seed: String,
    @SerializedName("sk")
    val sk: String
)