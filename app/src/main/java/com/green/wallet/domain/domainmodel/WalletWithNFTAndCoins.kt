package com.green.wallet.domain.domainmodel

data class WalletWithNFTAndCoins(
	val mnemonics: List<String>,
	val fingerPrint: Long,
	val address: String,
	val observer: Int,
	val non_observer: Int,
	val nftMap: HashMap<NFTCoin, NFTInfo>
)
