package com.green.wallet.presentation.main.dapp.trade.params

data class AssetAmount(
    val assetId: String? = null,
    val amount: String? = null
)

data class CreateOfferParams(
    val offerAssets: List<AssetAmount>? = null,
    val requestAssets: List<AssetAmount>? = null,
    val fee: String? = null
)

