package com.green.wallet.data.network.dto.firebase

import com.green.wallet.domain.domainmodel.DAppLink

data class DAppLinkFirebase(
    val name: String,
    val category: String,
    val icon: String,
    val link: String,
    val verified: Boolean,
    val description: String
) {

    constructor() : this("", "", "", "", false, description = "")

    fun toDAppLink() = DAppLink(
        name = name,
        category = category.split(",").map { it.trim() },
        imgUrl = icon,
        url = link,
        isVerified = verified,
        description = description
    )

}