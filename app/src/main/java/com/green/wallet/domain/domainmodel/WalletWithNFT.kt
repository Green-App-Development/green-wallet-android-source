package com.green.wallet.domain.domainmodel

class WalletWithNFT(
	val mnemonics: List<String>,
	val fingerPrint: Int,
	val address: String,
	val observer: Int,
	val non_observer: Int,
	val nftList: List<NFTInfo>
)
