package com.android.greenapp.presentation.main.send.spend

data class SpendBundle(
    val aggregated_signature: String,
    val coin_spends: List<CoinSpend>
)