package com.green.wallet.data.local.relations

import androidx.room.Relation
import com.green.wallet.data.local.entity.NFTCoinEntity

data class WalletWithNFT(
	val fingerPrint: Long,
	val mnemonics: String,
	val observer_hash: Int,
	val non_observer_hash: Int,
	val address: String,
	@Relation(
		parentColumn = "address",
		entityColumn = "address_fk"
	)
	val nftCoins: List<NFTCoinEntity>

)
