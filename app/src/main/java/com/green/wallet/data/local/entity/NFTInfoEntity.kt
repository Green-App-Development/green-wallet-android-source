package com.green.wallet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity()
data class NFTInfoEntity(
	@PrimaryKey(autoGenerate = false)
	val nft_coin_hash: String,
	val launcher_id:String,
	val owner_did:String,
	val minter_did:String,
	val royalty_percentage:Int,
	val mint_height:Int,
	val
	)
