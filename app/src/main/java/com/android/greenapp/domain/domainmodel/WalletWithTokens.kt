package com.android.greenapp.domain.domainmodel


data class WalletWithTokens(
	val networkType: String,
	val totalAmountInUSD: Double,
	val fingerPrint: Long,
	val tokenWalletList: List<TokenWallet>,
	val mnemonics:List<String>,
	val puzzle_hashes:List<String>,
	val address:String,
	val observer:Int,
	val nonObserver:Int
)
