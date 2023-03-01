package com.green.wallet.presentation.main.managewallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.green.wallet.domain.domainmodel.Wallet
import com.green.wallet.domain.interact.BlockChainInteract
import com.green.wallet.domain.interact.WalletInteract
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


class ManageWalletViewModel @Inject constructor(
	private val walletInteract: WalletInteract,
	private val blockChainInteract: BlockChainInteract
) :
	ViewModel() {

	fun getFlowAllWalletListFirstHomeIsAddedThenRemain() =
		walletInteract.getAllWalletListFirstHomeIsAddedThenRemainFlow()

	suspend fun getAllWalletList() = walletInteract.getAllWalletList()

	private val _walletList = MutableStateFlow<List<Wallet>?>(null)
	val walletList = _walletList.asStateFlow()

	private var job: Job? = null

	fun deleteWallet(wallet: Wallet) {
		viewModelScope.launch {
			walletInteract.deleteWallet(wallet)
		}
	}

}
