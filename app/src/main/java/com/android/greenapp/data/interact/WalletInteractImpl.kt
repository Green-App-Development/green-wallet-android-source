package com.android.greenapp.data.interact

import android.os.Build
import androidx.annotation.RequiresApi
import com.android.greenapp.data.local.TokenDao
import com.android.greenapp.data.local.TransactionDao
import com.android.greenapp.data.local.WalletDao
import com.android.greenapp.data.local.entity.WalletEntity
import com.android.greenapp.data.preference.PrefsManager
import com.android.greenapp.domain.domainmodel.TokenWallet
import com.android.greenapp.domain.domainmodel.Wallet
import com.android.greenapp.domain.domainmodel.WalletWithTokens
import com.android.greenapp.domain.interact.BlockChainInteract
import com.android.greenapp.domain.interact.PrefsInteract
import com.android.greenapp.domain.interact.WalletInteract
import com.android.greenapp.presentation.custom.*
import com.android.greenapp.presentation.tools.NetworkType
import com.example.common.tools.VLog
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import java.util.*
import javax.inject.Inject

/**
 * Created by bekjan on 09.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class WalletInteractImpl @Inject constructor(
	private val walletDao: WalletDao,
	private val prefsInteract: PrefsInteract,
	private val transactionDao: TransactionDao,
	private val tokenDao: TokenDao,
	private val encryptor: AESEncryptor,
	private val blockChainInteract: BlockChainInteract
) : WalletInteract {

	override fun getFlowOfWalletList(): Flow<List<Wallet>> {
		return walletDao.getFlowOfAllWalletList()
			.map { list -> list.map { it.toWallet(getDecryptedMnemonicsList(it.encMnemonics)) } }
	}

	private suspend fun getDecryptedMnemonicsList(encMnemonics: String): List<String> {
		try {
			val secretKeySpec =
				encryptor.getAESKey(prefsInteract.getSettingString(PrefsManager.PASSCODE, ""))
			val decMnemonics = encryptor.decrypt(encMnemonics, secretKeySpec!!)
			return getListFromString(decMnemonics)
		} catch (ex: Exception) {
			VLog.d("Exception in decrypting mnemonics : ${ex.message}")
		}
		return listOf()
	}

	private fun getListFromString(str: String): List<String> {
		val withoutBrakes = str.substring(1, str.length - 1)
		return withoutBrakes.split(",").map { it.trim() }.toList()
	}

	override suspend fun getAllWalletList(): List<Wallet> {
		val walletList = walletDao.getAllWalletList().map {
			convertWalletEntityToWallet(it)
		}
		return walletList
	}

	private suspend fun convertWalletEntityToWallet(walletEntity: WalletEntity): Wallet {
		if (isThisChivesNetwork(walletEntity.networkType)) {
			val wallet = walletEntity.toWallet(getDecryptedMnemonicsList(walletEntity.encMnemonics))
			wallet.balanceInUSD = prefsInteract.getCoursePriceDouble(
				getPreferenceKeyForCurStockNetworkDouble(wallet.networkType.split(" ")[0]), 0.0
			)
			return wallet
		}

		val amount = walletDao.getWalletByFingerPrint(walletEntity.fingerPrint)[0].balance
		val wallet = walletEntity.toWallet(getDecryptedMnemonicsList(walletEntity.encMnemonics))
		wallet.balance = amount
		wallet.balanceInUSD = amount * prefsInteract.getCoursePriceDouble(
			getPreferenceKeyForCurStockNetworkDouble(wallet.networkType.split(" ")[0]), 0.0
		)
		return wallet
	}

	override fun getHomeAddedWalletWithTokensFlow(): Flow<List<WalletWithTokens>> {

		val walletWithTokens = walletDao.getWalletListHomeIsAdded().map { walletEntityList ->
			walletEntityList.map { convertWalletEntityToWalletWithTokens(it) }
		}
		return walletWithTokens
	}

	private suspend fun convertWalletEntityToWalletWithTokens(walletEntity: WalletEntity): WalletWithTokens {
		val wallet = walletEntity.toWallet(getDecryptedMnemonicsList(walletEntity.encMnemonics))
		if (isThisChivesNetwork(walletEntity.networkType)) {
			val balanceUSD = wallet.balance * prefsInteract.getCoursePriceDouble(
				getPreferenceKeyForCurStockNetworkDouble(walletEntity.networkType.split(" ")[0]),
				0.0
			)
			val tokenWallet = TokenWallet(
				wallet.networkType,
				getShortNetworkType(wallet.networkType),
				wallet.balance,
				balanceUSD,
				"",
				""
			)
			return WalletWithTokens(
				wallet.networkType,
				balanceUSD,
				wallet.fingerPrint,
				listOf(tokenWallet),
				wallet.mnemonics,
				wallet.sk,
				wallet.address
			)
		}
		VLog.d("Converting walletEntity to walletWithTokens  -> $walletEntity")
		val hashWithAmount = walletEntity.hashWithAmount
		val hashListMutList = walletEntity.hashListImported.keys.toMutableList()
		val tokensDefault = tokenDao.getTokensDefaultOnScreen().map { it.hash }
		val assetIds = mutableListOf<AssetIDWithPriority>()
		for (hash in hashListMutList) {
			val priority = if (tokensDefault.contains(hash)) {
				if (hash == "GAD") 2
				else 1
			} else
				0
			assetIds.add(AssetIDWithPriority(hash, priority))
		}

		val hashList = assetIds.sortedWith(object : Comparator<AssetIDWithPriority> {
			override fun compare(p0: AssetIDWithPriority, p1: AssetIDWithPriority): Int {
				return p1.priority - p0.priority
			}
		}).map { it.asset_id }

		val tokenList = mutableListOf<TokenWallet>()
		val amountChia = walletEntity.balance
		val chiaAmountInUSD = prefsInteract.getCoursePriceDouble(
			getPreferenceKeyForCurStockNetworkDouble("Chia"),
			0.0
		) * amountChia
		tokenList.add(
			TokenWallet(
				wallet.networkType,
				getShortNetworkType(wallet.networkType),
				amountChia,
				chiaAmountInUSD,
				"",
				""
			)
		)
		var totalAmountInUSD = chiaAmountInUSD
		for (hash in hashList) {
			val tokenOpt = tokenDao.getTokenByHash(hash)
			if (tokenOpt.isPresent) {
				val token = tokenOpt.get()
				val amount = hashWithAmount[hash] ?: 0.0
				val tokenPrice = token.price
				val curTotalAmountInUSD = tokenPrice * amount
				VLog.d("TokenPrice for $token is $tokenPrice in walletInteractImpl")
				val tokenWallet = TokenWallet(
					token.name,
					token.code,
					amount,
					amount * tokenPrice,
					token.logo_url,
					token.hash
				)
				totalAmountInUSD += curTotalAmountInUSD
				if (amount == 0.0 && !token.enabled)
					continue
				tokenList.add(tokenWallet)
			} else {
				VLog.d("Given hash doesn't exist in tokens db : $hash")
			}
		}
		return WalletWithTokens(
			walletEntity.networkType,
			totalAmountInUSD,
			wallet.fingerPrint,
			tokenList,
			wallet.mnemonics,
			wallet.sk,
			wallet.address
		)
	}

	override suspend fun deleteWallet(wallet: Wallet): Int {
		val rowDeleted = walletDao.deleteWalletByAddress(wallet.address)
		transactionDao.deleteTransactionsWhenWalletDeleted(wallet.address)

		if (wallet.home_id_added != 0L) {
			prefsInteract.decreaseHomeAddedCounter()
		}
		val counter = prefsInteract.getHomeAddedCounter()
		if (counter == 0) {
			addRandomWalletToHome()
		}
		return rowDeleted
	}

	@RequiresApi(Build.VERSION_CODES.N)
	private suspend fun addRandomWalletToHome() {
		val optWallet = walletDao.getRandomWallet()
		if (optWallet.isPresent) {
			walletDao.updateHomeIsAdded(System.currentTimeMillis(), optWallet.get().fingerPrint)
			prefsInteract.increaseHomeAddedCounter()
		}
	}

	@RequiresApi(Build.VERSION_CODES.N)
	override suspend fun getWalletByFingerPrint(id: Long) =
		walletDao.getWalletByFingerPrint(id)
			.map { it.toWallet(getDecryptedMnemonicsList(it.encMnemonics)) }

	override suspend fun update_home_is_added(time: Long, fingerPrint: Long) =
		walletDao.updateHomeIsAdded(time, fingerPrint)

	override suspend fun getWalletListByNetworkTypeFingerPrint(
		networkType: String,
		fingerPrint: Long?
	): List<Wallet> {

		val walletList =
			walletDao.getWalletListByNetworkTypeAndFingerPrint(networkType, fingerPrint)
				.map {
					convertWalletEntityToWalletWithAmountInUSD(it)
				}
		return walletList
	}

	private suspend fun convertWalletEntityToWalletWithAmountInUSD(it: WalletEntity): Wallet {
		VLog.d("Balance for fingerPrint : ${it.fingerPrint}  : balance : ${it.balance} and HashWithAmount : ${it.hashWithAmount}")
		var totalBal = it.balance
		if (!isThisChivesNetwork(it.networkType))
			totalBal = it.hashWithAmount[it.networkType] ?: 0.0
		val balanceInUSD = prefsInteract.getCoursePriceDouble(
			getPreferenceKeyForCurStockNetworkDouble(
				it.networkType.split(" ")[0]
			), 0.0
		) * totalBal
		val wallet = it.toWallet(getDecryptedMnemonicsList(it.encMnemonics))
		wallet.balanceInUSD = balanceInUSD
		wallet.balance = totalBal
		return wallet
	}

	override suspend fun getRandomWalletByNetworktype(networkType: NetworkType) =
		walletDao.getRandomWalletByNetworkType(networkType)
			.map { it.toWallet(getDecryptedMnemonicsList(it.encMnemonics)) }

	override suspend fun getDistinctNetworkTypes(): List<String> {
		return walletDao.getDistinctNetworkTypes()
	}

	override suspend fun checkIfMnemonicsExistInDB(
		mnemonics: List<String>,
		networkType: String
	): Optional<Boolean> {
		val key = prefsInteract.getSettingString(PrefsManager.PASSCODE, "")
		val encMnemonics = encryptor.encrypt(mnemonics.toString(), encryptor.getAESKey(key)!!)
		val walletOpt = walletDao.checkIfEncMnemonicsExistInDB(encMnemonics, networkType)
		if (walletOpt.isPresent)
			return Optional.of(true)
		return Optional.of(false)
	}

	override fun getAllWalletListFirstHomeIsAddedThenRemain(): Flow<List<Wallet>> {
		val homeAddedWalletList =
			walletDao.getWalletListHomeAddedFirstThenRemaining().map { walletEntityList ->
				VLog.d("WalletList Changed for On Manage Fragment : $walletEntityList")
				walletEntityList.map {
					convertWalletEntityToWallet(it)
				}
			}
		return homeAddedWalletList
	}

	override suspend fun getWalletWithTokensByFingerPrintNetworkType(
		fingerPrint: Long?,
		networkType: String
	): List<WalletWithTokens> {
		val walletTokenList =
			walletDao.getWalletListByNetworkTypeAndFingerPrint(networkType, fingerPrint).map {
				convertWalletEntityToWalletWithTokens(it)
			}
		return walletTokenList
	}

	override suspend fun importTokenByFingerPrint(
		fingerPrint: Long,
		add: Boolean,
		asset_id: String,
		outer_puzzle_hash: String
	) {
		val walletEntity = walletDao.getWalletByFingerPrint(fingerPrint)[0]
		val hashListImported = walletEntity.hashListImported
		if (add)
			hashListImported[asset_id] = outer_puzzle_hash
		else {
			hashListImported.remove(asset_id)
		}
		walletDao.updateChiaNetworkHashListImportedByFingerPrint(fingerPrint, hashListImported)
	}

	override suspend fun importTokenByAddress(
		address: String,
		add: Boolean,
		asset_id: String,
		outer_puzzle_hash: String
	) {
		val walletEntity = walletDao.getWalletByAddress(address)[0]
		val hashListImported = walletEntity.hashListImported
		if (add) {
			CoroutineScope(Dispatchers.IO).launch {
				hashListImported[asset_id] = outer_puzzle_hash
				blockChainInteract.updateTokenBalanceWithFullNode(walletEntity)
			}
		} else {
			hashListImported.remove(asset_id)
		}
		walletDao.updateChiaNetworkHashListImportedByAddress(address, hashListImported)
	}

	data class AssetIDWithPriority(val asset_id: String, val priority: Int)

}
