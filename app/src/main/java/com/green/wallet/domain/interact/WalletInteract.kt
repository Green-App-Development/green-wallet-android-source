package com.green.wallet.domain.interact

import com.green.wallet.domain.domainmodel.Wallet
import com.green.wallet.domain.domainmodel.WalletWithTokens
import com.green.wallet.presentation.tools.NetworkType
import kotlinx.coroutines.flow.Flow
import java.util.*
import kotlin.collections.HashMap

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

	fun getAllWalletListFirstHomeIsAddedThenRemainFlow(): Flow<List<Wallet>>


	suspend fun getAllWalletListFirstHomeIsAddedThenRemain(): List<Wallet>

	suspend fun getWalletWithTokensByFingerPrintNetworkType(
		fingerPrint: Long?,
		networkType: String
	): List<WalletWithTokens>


	fun getWalletWithTokensByFingerPrintNetworkTypeFlow(
		fingerPrint: Long?,
		networkType: String
	): Flow<List<WalletWithTokens>>


	suspend fun importTokenByAddress(
		address: String,
		add: Boolean,
		asset_id: String,
		outer_puzzle_hashes: List<String>
	)

	suspend fun getWalletByAddress(address: String): Wallet

	suspend fun updateHashListImported(
		address: String,
		main_puzzle_hashes: List<String>,
		hashListImported: HashMap<String, List<String>>,
		observer:Int,
		nonObserver:Int
	)


}
