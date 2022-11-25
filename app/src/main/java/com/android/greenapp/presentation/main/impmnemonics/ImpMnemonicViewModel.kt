package com.android.greenapp.presentation.main.impmnemonics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.greenapp.domain.domainmodel.Wallet
import com.android.greenapp.domain.interact.*
import com.android.greenapp.presentation.tools.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ImpMnemonicViewModel @Inject constructor(
	private val prefs: PrefsInteract,
	private val blockChainInteract: BlockChainInteract,
	private val walletInteract: WalletInteract,
	private val tokenInteract: TokenInteract,
	private val cryptocurrencyInteract: CryptocurrencyInteract
) : ViewModel() {


	init {
		viewModelScope.launch {
			cryptocurrencyInteract.getAllTails()
		}
	}

	fun importNewWallet(wallet: Wallet, callBack: () -> Unit) {
		viewModelScope.launch {
			blockChainInteract.saveNewWallet(wallet = wallet, imported = true)
			callBack()
		}
	}

	suspend fun checkIfMnemonicsExist(mnemonics: List<String>, networkType: String) =
		walletInteract.checkIfMnemonicsExistInDB(mnemonics, networkType)

	suspend fun getTokenDefaultOnMainScreen() = tokenInteract.getTokenListDefaultOnMainScreen()


}
