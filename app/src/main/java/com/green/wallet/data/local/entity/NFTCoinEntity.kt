package com.green.wallet.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class NFTCoinEntity(
	@PrimaryKey(false)
	val coin_info: String,
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
