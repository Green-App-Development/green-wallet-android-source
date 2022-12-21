package com.green.wallet.domain.domainmodel

/**
 * Created by bekjan on 20.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */
data class CurrencyItem(val price: Double, val percent: String, val increased: Boolean) {
    var network = ""
}
