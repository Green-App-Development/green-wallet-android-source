package com.green.wallet.data.network.dto.nft

data class NftInfoDto(
    val chain_info: String?,
    val data_hash: String?,
    val data_uris: List<String>,
    val edition_number: Int,
    val edition_total: Int,
    val launcher_id: String?,
    val launcher_puzhash: String?,
    val license_hash: String?,
    val license_uris: List<String>,
    val metadata_hash: String,
    val metadata_uris: List<String>,
    val mint_height: Int,
    val minter_did: String?,
    val nft_coin_confirmation_height: Int,
    val nft_coin_id: String?,
    val nft_id: String?,
    val off_chain_metadata: Any,
    val owner_did: String?,
    val p2_address: String?,
    val pending_transaction: Boolean,
    val royalty_percentage: Int,
    val royalty_puzzle_hash: String?,
    val supports_did: Boolean,
    val updater_puzhash: String?
)
