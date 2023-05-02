package com.green.wallet.domain.domainmodel

data class WalletWithNFTInfo(
	val fingerPrint: Long,
	val address: String,
	val nftInfos: List<NFTInfo>
)
