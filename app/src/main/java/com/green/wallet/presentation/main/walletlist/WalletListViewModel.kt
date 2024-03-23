package com.green.wallet.presentation.main.walletlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.green.wallet.domain.domainmodel.Wallet
import com.green.wallet.domain.interact.BlockChainInteract
import com.green.wallet.domain.interact.GreenAppInteract
import com.green.wallet.domain.interact.PrefsInteract
import com.green.wallet.domain.interact.SpentCoinsInteract
import com.green.wallet.domain.interact.WalletInteract
import com.green.wallet.presentation.main.pincode.PinCodeCommunicator
import com.green.wallet.presentation.tools.ReasonEnterCode
import com.greenwallet.core.base.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


class WalletListViewModel @Inject constructor(
    private val spentCoinsInteract: SpentCoinsInteract,
    private val walletInteract: WalletInteract,
    private val blockChainInteract: BlockChainInteract,
    private val prefs: PrefsInteract,
    private val greenAppInteract: GreenAppInteract,
    private val pinCodeCommunicator: PinCodeCommunicator
) :
    BaseViewModel<Unit, ListWalletEvent>(Unit) {

    private val _walletList = MutableStateFlow<List<Wallet>?>(null)
    val walletList: Flow<List<Wallet>?> = _walletList
    private var allListJob: Job? = null

    var wallet: Wallet? = null

    init {
        pinCodeCommunicator.onSuccessPassCode = {
            when (it) {
                ReasonEnterCode.REMOVE_WALLET -> {
                    wallet?.let {
                        deleteWallet(it)
                    }
                    setEvent(ListWalletEvent.PinCodeConfirmToDelete)
                }

                else -> Unit
            }
        }
    }

    suspend fun getAllWalletListFirstHomeIsAddedThenRemain() =
        walletInteract.getAllWalletListFirstHomeIsAddedThenRemain()

    private fun deleteWallet(wallet: Wallet) {
        viewModelScope.launch {
            walletInteract.deleteWallet(wallet)
            spentCoinsInteract.deleteSpentCoinsByAddressFk(wallet.address)
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
