package com.green.wallet.presentation.main.nft.nftsend

data class NftSendViewState(
    val fee: Double = 0.0,
    val dexieFee: Double = 0.0,
    val spendableBalance: Double = 0.0,
    val feeEnough: Boolean = false,
    val destAddress: String = ""
)