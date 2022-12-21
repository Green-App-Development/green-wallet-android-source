package com.green.wallet.presentation.main.walletlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.green.wallet.domain.domainmodel.Wallet
import com.green.wallet.domain.interact.BlockChainInteract
import com.green.wallet.domain.interact.GreenAppInteract
import com.green.wallet.domain.interact.PrefsInteract
import com.green.wallet.domain.interact.WalletInteract
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by bekjan on 09.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class WalletListViewModel @Inject constructor(
	private val walletInteract: WalletInteract,
	private val blockChainInteract: BlockChainInteract,
	private val prefs: PrefsInteract,
	private val greenAppInteract: GreenAppInteract
) :
	ViewModel() {

	private val _walletList = MutableStateFlow<List<Wallet>?>(null)
	val walletList: Flow<List<Wallet>?> = _walletList
	private var allListJob: Job? = null

	init {

	}

	suspend fun getAllWalletListFirstHomeIsAddedThenRemain() =
		walletInteract.getAllWalletListFirstHomeIsAddedThenRemain()

	fun deleteWallet(wallet: Wallet) {
		viewModelScope.launch {
			walletInteract.deleteWallet(wallet)
		}
	}

	fun updateHomeIdAdded(time: Long, fingerPrint: Long) {
		viewModelScope.launch {
			walletInteract.update_home_is_added(time, fingerPrint)
		}
	}


	suspend fun getHomeAddCounter() = prefs.getHomeAddedCounter()

	suspend fun increaseHomeAddCounter() = prefs.increaseHomeAddedCounter()

	suspend fun decreaseHomeAddCounter() = prefs.decreaseHomeAddedCounter()


}
