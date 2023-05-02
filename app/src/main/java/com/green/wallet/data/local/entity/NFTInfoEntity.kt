package com.green.wallet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.green.wallet.domain.domainmodel.NFTInfo


@Entity(
    tableName = "NFTInfoEntity"
)
data class NFTInfoEntity(
    @PrimaryKey(autoGenerate = false)
    val nft_coin_hash: String,
    val nft_id: String,
    val launcher_id: String,
    val owner_did: String,
    val minter_did: String,
    val royalty_percentage: Int,
    val mint_height: Int,
    val data_url: String,
    val data_hash: String,
    val meta_hash: String,
    val meta_url: String,
    val description: String,
    val collection: String,
    val properties: HashMap<String, String>,
	val name:String
) {
    fun toNFTInfo() = NFTInfo(
        nft_coin_hash,
        nft_id,
        launcher_id,
        owner_did,
        minter_did,
        royalty_percentage,
        mint_height,
        data_url,
        data_hash,
        meta_hash,
        meta_url,
        description,
        collection,
        properties,
		name
    )
}
