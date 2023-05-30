package com.green.wallet.data.local.dto

import com.green.wallet.domain.domainmodel.ChiaWallet

data class ChiaWalletDTO(
	val fingerPrint: Long,
	val address: String
) {

	fun toChiaWallet() = ChiaWallet(fingerPrint, address)

}
