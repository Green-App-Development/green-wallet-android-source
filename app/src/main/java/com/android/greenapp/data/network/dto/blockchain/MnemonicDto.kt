package com.android.greenapp.data.network.dto.blockchain

import com.google.gson.annotations.SerializedName

data class MnemonicDto(
    @SerializedName("mnemonic")
    val mnemonic: List<String>,
    @SerializedName("success")
    val success: Boolean
)