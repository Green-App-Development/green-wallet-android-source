package com.android.greenapp.presentation.main.managewallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.greenapp.domain.domainmodel.Wallet
import com.android.greenapp.domain.interact.BlockChainInteract
import com.android.greenapp.domain.interact.WalletInteract
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by bekjan on 10.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class ManageWalletViewModel @Inject constructor(
	private val walletInteract: WalletInteract,
	private val blockChainInteract: BlockChainInteract
) :
	ViewModel() {

	fun getFlowAllWalletListFirstHomeIsAddedThenRemain() =
		walletInteract.getAllWalletListFirstHomeIsAddedThenRemain()

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
