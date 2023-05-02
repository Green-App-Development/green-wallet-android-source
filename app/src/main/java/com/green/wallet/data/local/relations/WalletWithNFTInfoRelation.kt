package com.green.wallet.data.local.relations

import androidx.room.Relation
import com.green.wallet.data.local.entity.NFTInfoEntity

class WalletWithNFTInfoRelation(
	val fingerPrint: Long,
	val address: String,
	@Relation(
		parentColumn = "address", entityColumn = "address_fk"
	) val nftInfos: List<NFTInfoEntity>
)
