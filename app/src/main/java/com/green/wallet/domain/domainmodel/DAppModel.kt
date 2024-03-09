package com.green.wallet.domain.domainmodel

import androidx.annotation.DrawableRes

data class DAppModel(
    val name: String,
    val description: String,
    val url: String,
    @DrawableRes val resource: Int,
    val isConnected: Boolean = false
)