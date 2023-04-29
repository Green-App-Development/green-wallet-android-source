package com.green.wallet.domain.domainmodel

class WalletWithNFTAndCoins(
	val mnemonics: List<String>,
	val fingerPrint: Long,
	val address: String,
	val observer: Int,
	val non_observer: Int,
	val nftInfoList: List<NFTInfo>,
	val nftCoinList:List<NFTCoin>
)
