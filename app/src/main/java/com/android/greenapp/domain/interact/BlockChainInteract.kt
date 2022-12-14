package com.android.greenapp.domain.interact

import com.android.greenapp.data.local.entity.WalletEntity
import com.android.greenapp.data.network.BlockChainService
import com.android.greenapp.domain.domainmodel.Wallet
import com.android.greenapp.presentation.tools.Resource
import java.util.HashMap

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
