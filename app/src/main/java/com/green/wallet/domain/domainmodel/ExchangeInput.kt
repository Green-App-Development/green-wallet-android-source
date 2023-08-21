package com.green.wallet.domain.domainmodel

class ExchangeInput(
    val getCoin: String,
    val giveCoin: String,
    val rate: String,
    val amount: String,
    val feeNetwork: Double
)