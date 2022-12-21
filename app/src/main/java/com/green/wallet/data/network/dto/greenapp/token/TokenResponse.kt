package com.green.wallet.data.network.dto.greenapp.token



data class TokenResponse(
    val version: String,
    val list: List<TokenDto>
)
