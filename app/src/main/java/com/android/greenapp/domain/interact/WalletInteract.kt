package com.android.greenapp.domain.interact

import com.android.greenapp.domain.entity.Wallet
import com.android.greenapp.domain.entity.WalletWithTokens
import com.android.greenapp.presentation.tools.NetworkType
import com.android.greenapp.presentation.tools.Resource
import kotlinx.coroutines.flow.Flow
import java.util.*

/**
 * Created by bekjan on 09.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */
interface WalletInteract {

	fun getFlowOfWalletList(): Flow<List<Wallet>>

	suspend fun getAllWalletList(): List<Wallet>

	fun getHomeAddedWalletWithTokensFlow(): Flow<List<WalletWithTokens>>

	suspend fun deleteWallet(wallet: Wallet): Int

	suspend fun getWalletByFingerPrint(id: Long): List<Wallet>

	suspend fun update_home_is_added(time: Long, fingerPrint: Long): Int

	suspend fun getWalletListByNetworkTypeFingerPrint(
		networkType: String,
		fingerPrint: Long?
	): List<Wallet>

	suspend fun getRandomWalletByNetworktype(networkType: NetworkType): List<Wallet>

	suspend fun getDistinctNetworkTypes(): List<String>

	suspend fun checkIfMnemonicsExistInDB(
		mnemonics: List<String>,
		networkType: String
	): Optional<Boolean>

	fun getAllWalletListFirstHomeIsAddedThenRemain(): Flow<List<Wallet>>

	suspend fun getWalletWithTokensByFingerPrintNetworkType(
		fingerPrint: Long?,
		networkType: String
	): List<WalletWithTokens>

	suspend fun importTokenByFingerPrint(
		fingerPrint: Long,
		add: Boolean,
		asset_id: String,
		outer_puzzle_hash: String
	)

	suspend fun importTokenByAddress(
		address: String,
		add: Boolean,
		asset_id: String,
		outer_puzzle_hash: String
	)
}
