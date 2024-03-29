package com.green.wallet.data.local.relations

import androidx.room.Relation
import com.green.wallet.data.local.entity.NFTCoinEntity

class WalletWithNFTCoin(
	val fingerPrint: Long,
	val mnemonics: String,
	val address: String,
	@Relation(
		parentColumn = "address", entityColumn = "address_fk"
	) val nftCoins: List<NFTCoinEntity>

)
