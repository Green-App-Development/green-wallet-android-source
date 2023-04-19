package com.green.wallet.presentation.main.impmnemonics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.green.wallet.domain.domainmodel.Wallet
import com.green.wallet.domain.interact.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import javax.inject.Inject

class ImpMnemonicViewModel @Inject constructor(
	private val prefs: PrefsInteract,
	private val blockChainInteract: BlockChainInteract,
	private val walletInteract: WalletInteract,
	private val tokenInteract: TokenInteract,
	private val cryptocurrencyInteract: CryptocurrencyInteract
) : ViewModel() {

	private val handler = CoroutineExceptionHandler { _, thr ->

	}

	init {
		viewModelScope.launch(handler) {
			cryptocurrencyInteract.getAllTails()
		}
	}

	fun importNewWallet(wallet: Wallet, callBack: () -> Unit) {
		viewModelScope.launch(handler) {
			blockChainInteract.saveNewWallet(wallet = wallet, imported = true)
			callBack()
		}
	}

	suspend fun checkIfMnemonicsExist(mnemonics: List<String>, networkType: String) =
		walletInteract.checkIfMnemonicsExistInDB(mnemonics, networkType)

	suspend fun getTokenDefaultOnMainScreen() = tokenInteract.getTokenListDefaultOnMainScreen()


}
