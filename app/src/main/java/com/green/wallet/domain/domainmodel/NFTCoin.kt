package com.green.wallet.domain.domainmodel

import androidx.room.PrimaryKey

data class NFTCoin(
    val coin_info: String,
    val address_fk: String,
    val coin_hash: String,
    val amount: Int,
    val confirmed_block_index: Long,
    val spent_block_index: Long,
    val time_stamp: Long,
    val parent_coin_info: String,
    val parent_coin_hash: String,
    val puzzle_reveal: String,
    val solution: String
)