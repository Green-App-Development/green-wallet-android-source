package com.green.wallet.domain.domainmodel

data class DAppLink(
    val name: String,
    val description: String,
    val url: String,
    val imgUrl: String,
    val isConnected: Boolean = false,
    val category: List<String>,
    val isVerified: Boolean
)