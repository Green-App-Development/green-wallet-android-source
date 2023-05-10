package com.green.wallet.presentation.main.nft.nftsend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.green.wallet.data.network.dto.greenapp.network.NetworkItem
import com.green.wallet.data.preference.PrefsManager
import com.green.wallet.domain.domainmodel.*
import com.green.wallet.domain.interact.*
import com.green.wallet.presentation.custom.formattedDoubleAmountWithPrecision
import com.green.wallet.presentation.custom.getPreferenceKeyForNetworkItem
import com.green.wallet.presentation.tools.NetworkType
import com.green.wallet.presentation.tools.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject


class NFTSendViewModel @Inject constructor(
	private val nftInteract: NFTInteract,
	private val walletInteract: WalletInteract,
	private val spentCoinsInteract: SpentCoinsInteract,
	private val prefsManager: PrefsManager,
	private val blockChainInteract: BlockChainInteract,
	private val addressInteract: AddressInteract,
) : ViewModel() {

	lateinit var wallet: Wallet
	lateinit var nftCoin: NFTCoin
	var alreadySpentCoins = listOf<SpentCoin>()
	lateinit var base_url: String
	private val _spendableBalance = MutableStateFlow("0")
	val spendableBalance = _spendableBalance.asStateFlow()

	init {

	}

	fun initNFTCoin(nftInfo: NFTInfo) {
		viewModelScope.launch {
			wallet = walletInteract.getWalletByAddress(nftInfo.fk_address)
			nftCoin = nftInteract.getNFTCoinByHash(nftInfo.nft_coin_hash)
			alreadySpentCoins = spentCoinsInteract.getSpentCoinsToPushTrans(
				wallet.networkType,
				wallet.address,
				"XCH"
			)
			val item =
				prefsManager.getObjectString(getPreferenceKeyForNetworkItem(wallet.networkType))
			base_url = Gson().fromJson(item, NetworkItem::class.java).full_node
			initCalculateSpendableBalance(wallet)
		}
	}

	private suspend fun initCalculateSpendableBalance(wallet: Wallet) {
		spentCoinsInteract.getSumSpentCoinsForSpendableBalance(
			wallet.networkType,
			wallet.address,
			"XCH"
		).collectLatest {
			var bigDecimalSpendableAmount =
				(BigDecimal("${wallet.balance}").subtract(BigDecimal("${it[0]}"))).toDouble()
			if (bigDecimalSpendableAmount < 0.0)
				bigDecimalSpendableAmount = 0.0
			var spendableAmountString =
				formattedDoubleAmountWithPrecision(bigDecimalSpendableAmount)
			if (Math.round(bigDecimalSpendableAmount)
					.toDouble() == bigDecimalSpendableAmount || bigDecimalSpendableAmount == 0.0
			)
				spendableAmountString = "${Math.round(bigDecimalSpendableAmount)}"
			_spendableBalance.emit(spendableAmountString)
		}
	}

	var sendNFTState = MutableStateFlow<Resource<String>?>(null)
		private set

	suspend fun checkIfAddressExistInDb(address: String) =
		addressInteract.checkIfAddressAlreadyExist(address = address)

	suspend fun insertAddressEntity(address: Address) = addressInteract.insertAddressEntity(address)

	fun sendNFTBundle(
		spendBundleJson: String,
		destPuzzleHash: String,
		spentCoinsJson: String,
		nftInfo: NFTInfo,
		fee_amount: Double,
		confirm_height: Int,
		networkType: String
	) {
		viewModelScope.launch {
			val res = blockChainInteract.push_tx_nft(
				spendBundleJson,
				base_url,
				destPuzzleHash,
				nftInfo,
				spentCoinsJson,
				fee_amount,
				confirm_height,
				networkType
			)
			sendNFTState.value = res
		}
	}


}
