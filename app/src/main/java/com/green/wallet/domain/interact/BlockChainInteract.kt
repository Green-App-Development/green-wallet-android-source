package com.green.wallet.domain.interact

import com.green.wallet.data.local.entity.WalletEntity
import com.green.wallet.domain.domainmodel.Wallet
import com.green.wallet.presentation.tools.Resource

/**
 * Created by bekjan on 06.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */
interface BlockChainInteract {


	suspend fun saveNewWallet(
		wallet: Wallet,
		imported: Boolean
	): Resource<String>

	suspend fun updateBalanceAndTransactionsPeriodically()

	suspend fun push_tx(
		jsonSpendBundle: String,
		url: String,
		sendAmount: Double,
		networkType: String,
		fingerPrint: Long,
		code: String,
		dest_puzzle_hash: String,
		address: String,
		fee: Double,
		spentCoinsJson: String,
		spentCoinsToken: String
	): Resource<String>

	suspend fun updateTokenBalanceWithFullNode(wallet: WalletEntity)

	suspend fun updateWalletBalance(wallet: WalletEntity)


}
