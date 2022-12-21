package com.green.wallet.data.network.dto.greenapp.network

/**
 * Created by bekjan on 04.07.2022.
 * email: bekjan.omirzak98@gmail.com
 */

data class NetworkItem(
    val name: String,
    val full_node: String,
    val wallet: String,
    val daemon: String,
    val farmer: String,
    val harvester: String
)
