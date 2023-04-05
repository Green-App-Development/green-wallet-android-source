package com.green.wallet.data.interact

import android.os.Build
import androidx.annotation.RequiresApi
import com.green.wallet.data.local.SpentCoinsDao
import com.green.wallet.data.local.TokenDao
import com.green.wallet.data.local.TransactionDao
import com.green.wallet.data.local.WalletDao
import com.green.wallet.data.local.entity.WalletEntity
import com.green.wallet.data.preference.PrefsManager
import com.green.wallet.domain.domainmodel.TokenWallet
import com.green.wallet.domain.domainmodel.Wallet
import com.green.wallet.domain.domainmodel.WalletWithTokens
import com.green.wallet.domain.interact.BlockChainInteract
import com.green.wallet.domain.interact.PrefsInteract
import com.green.wallet.domain.interact.WalletInteract
import com.green.wallet.presentation.custom.*
import com.green.wallet.presentation.tools.NetworkType
import com.green.wallet.presentation.tools.VLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap


class WalletInteractImpl @Inject constructor(
	private val walletDao: WalletDao,
	private val prefsInteract: PrefsInteract,
	private val transactionDao: TransactionDao,
	private val tokenDao: TokenDao,
	private val encryptor: AESEncryptor,
	private val blockChainInteract: BlockChainInteract,
	private val spentCoinsDao: SpentCoinsDao
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
				wallet.puzzle_hashes,
				wallet.address,
				wallet.observerHash,
				wallet.nonObserverHash
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
			wallet.puzzle_hashes,
			wallet.address,
			wallet.observerHash,
			wallet.nonObserverHash
		)
	}

	override suspend fun deleteWallet(wallet: Wallet): Int {
		val rowDeleted = walletDao.deleteWalletByAddress(wallet.address)
		transactionDao.deleteTransactionsWhenWalletDeleted(wallet.address)
		spentCoinsDao.deleteSpentCoinsByFkAddress(wallet.address)

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

	override fun getAllWalletListFirstHomeIsAddedThenRemainFlow(): Flow<List<Wallet>> {
		val homeAddedWalletList =
			walletDao.getWalletListHomeAddedFirstThenRemainingFlow().map { walletEntityList ->
				VLog.d("WalletList Changed for On Manage Fragment : $walletEntityList")
				walletEntityList.map {
					convertWalletEntityToWallet(it)
				}
			}
		return homeAddedWalletList
	}

	override suspend fun getAllWalletListFirstHomeIsAddedThenRemain(): List<Wallet> {
		return walletDao.getWalletListHomeAddedFirstThenRemaining().map { walletEntity ->
			convertWalletEntityToWallet(walletEntity)
		}
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

	override fun getWalletWithTokensByFingerPrintNetworkTypeFlow(
		fingerPrint: Long?,
		networkType: String
	): Flow<List<WalletWithTokens>> {
		return walletDao.getWalletListByNetworkTypeAndFingerPrintFlow(networkType, fingerPrint)
			.map { list -> list.map { convertWalletEntityToWalletWithTokens(it) } }
	}

	override suspend fun importTokenByAddress(
		address: String,
		add: Boolean,
		asset_id: String,
		outer_puzzle_hashes: List<String>
	) {
		VLog.d("Updating import token by address : $address  : $outer_puzzle_hashes")
		val walletEntity = walletDao.getWalletByAddress(address)[0]
		val hashListImported = walletEntity.hashListImported
		if (add) {
			hashListImported[asset_id] = outer_puzzle_hashes
			blockChainInteract.updateTokenBalanceWithFullNode(walletEntity)
		} else {
			hashListImported.remove(asset_id)
		}
		walletDao.updateChiaNetworkHashListImportedByAddress(address, hashListImported)
	}

	override suspend fun getWalletByAddress(address: String): Wallet {
		val walletEntity = walletDao.getWalletByAddress(address = address).get(0)
		val wallet = walletEntity.toWallet(getDecryptedMnemonicsList(walletEntity.encMnemonics))
		return wallet
	}

	override suspend fun updateHashListImported(
		address: String,
		main_puzzle_hashes: List<String>,
		hashListImported: HashMap<String, List<String>>,
		observer: Int,
		nonObserver: Int
	) {
		walletDao.updateWalletMainPuzzleHashesByAddress(
			puzzle_hashes = main_puzzle_hashes,
			address = address
		)
		walletDao.updateWalletTokenPuzzleHashesByAddress(
			hashListImportedNew = hashListImported,
			address = address
		)
		walletDao.updateObserverHashCount(address, observer, nonObserver)
		val walletEntity = walletDao.getWalletByAddress(address = address)[0]
		blockChainInteract.updateWalletBalanceWithTransactions(walletEntity)
		blockChainInteract.updateTokenBalanceWithFullNode(walletEntity)
	}

	data class AssetIDWithPriority(val asset_id: String, val priority: Int)

}
