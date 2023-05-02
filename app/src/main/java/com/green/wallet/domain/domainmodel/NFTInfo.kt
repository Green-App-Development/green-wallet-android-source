package com.green.wallet.domain.domainmodel


data class NFTInfo(
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
)
