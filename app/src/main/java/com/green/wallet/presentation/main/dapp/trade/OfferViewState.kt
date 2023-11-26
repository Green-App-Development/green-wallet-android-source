package com.green.wallet.presentation.main.dapp.trade

data class OfferViewState(
    val acceptOffer: Boolean = false,
    val offeringCode: String = "",
    val offerAmount: Double = 0.0,
    val requestingCode: String = "",
    val requestingAmount: Double = 0.0,
    val offer: String = "",
    val requestingAssetId: String = ""
)