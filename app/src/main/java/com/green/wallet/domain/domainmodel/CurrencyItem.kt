package com.green.wallet.domain.domainmodel


data class CurrencyItem(val price: Double, val percent: String, val increased: Boolean) {
    var network = ""
}
