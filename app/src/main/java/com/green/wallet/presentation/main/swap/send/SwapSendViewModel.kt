package com.green.wallet.presentation.main.swap.send

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.green.wallet.domain.domainmodel.Address
import com.green.wallet.domain.domainmodel.Token
import com.green.wallet.domain.interact.AddressInteract
import com.green.wallet.domain.interact.BlockChainInteract
import com.green.wallet.domain.interact.GreenAppInteract
import com.green.wallet.domain.interact.SpentCoinsInteract
import com.green.wallet.domain.interact.TokenInteract
import com.green.wallet.domain.interact.WalletInteract
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class SwapSendViewModel @Inject constructor(
	private val walletInteract: WalletInteract,
	private val blockChainInteract: BlockChainInteract,
	private val addressInteract: AddressInteract,
	private val greenAppInteract: GreenAppInteract,
	private val tokenInteract: TokenInteract,
	private val spentCoinsInteract: SpentCoinsInteract,
) : ViewModel() {


	fun queryWalletWithTokensList(type: String, fingerPrint: Long?) =
		walletInteract.getWalletWithTokensByFingerPrintNetworkTypeFlow(fingerPrint, type)

	suspend fun push_transaction(
		spendBundle: String,
		url: String,
		amount: Double,
		networkType: String,
		fingerPrint: Long,
		code: String,
		dest_puzzle_hash: String,
		address: String,
		fee: Double,
		spentCoinsJson: String,
		spentCoinsToken: String
	) = blockChainInteract.push_tx(
		spendBundle,
		url,
		amount,
		networkType,
		fingerPrint,
		code,
		dest_puzzle_hash,
		address,
		fee,
		spentCoinsJson,
		spentCoinsToken
	)

	suspend fun checkIfAddressExistInDb(address: String) =
		addressInteract.checkIfAddressAlreadyExist(address = address)

	suspend fun getCoinDetailsFeeCommission(code: String) =
		greenAppInteract.getCoinDetails(code).fee_commission


	suspend fun getTokenPriceByCode(code: String) = tokenInteract.getTokenPriceByCode(code)

	suspend fun insertAddressEntity(address: Address) = addressInteract.insertAddressEntity(address)

	fun getSpentCoinsAmountsAddressCodeForSpendableBalance(
		address: String,
		tokenCode: String,
		networkType: String
	) =
		spentCoinsInteract.getSumSpentCoinsForSpendableBalance(networkType, address, tokenCode)


	suspend fun swipedRefreshLayout(onFinished: () -> Unit) {
		blockChainInteract.updateBalanceAndTransactionsPeriodically()
		greenAppInteract.requestOtherNotifItems()
	}

	suspend fun getSpentCoinsToPushTrans(networkType: String, address: String, tokenCode: String) =
		spentCoinsInteract.getSpentCoinsToPushTrans(networkType, address, tokenCode)

}
