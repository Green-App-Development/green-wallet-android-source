package com.green.wallet.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey
import com.green.wallet.domain.domainmodel.NFTCoin


@Entity(
	tableName = "NFTCoinEntity",
	foreignKeys = [
		ForeignKey(
			entity = WalletEntity::class,
			parentColumns = ["address"],
			childColumns = ["address_fk"],
			onDelete = CASCADE
		)
	]
)
data class NFTCoinEntity(
	@PrimaryKey(autoGenerate = false)
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
	val solution: String,
	val coin_base: Boolean
) {
	fun toNftCoin() = NFTCoin(
		coin_info,
		address_fk,
		coin_hash,
		amount,
		confirmed_block_index,
		spent_block_index,
		time_stamp,
		parent_coin_info,
		parent_coin_hash,
		puzzle_reveal,
		solution,
		coin_base
	)
}
