package com.android.greenapp.domain.entity


data class WalletWithTokens(
    val networkType: String,
    val totalAmountInUSD: Double,
    val fingerPrint: Long,
    val tokenWalletList: List<TokenWallet>,
    val mnemonics:List<String>,
	val main_puzzle_hash:String,
	val address:String
)
