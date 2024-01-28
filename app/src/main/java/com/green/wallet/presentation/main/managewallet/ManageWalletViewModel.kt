package com.green.wallet.presentation.main.managewallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.green.wallet.domain.domainmodel.Wallet
import com.green.wallet.domain.interact.BlockChainInteract
import com.green.wallet.domain.interact.WalletInteract
import com.green.wallet.presentation.main.pincode.PinCodeCommunicator
import com.green.wallet.presentation.tools.ReasonEnterCode
import com.greenwallet.core.base.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


class ManageWalletViewModel @Inject constructor(
    private val walletInteract: WalletInteract,
    private val blockChainInteract: BlockChainInteract,
    private val pinCodeCommunicator: PinCodeCommunicator
) :
    BaseViewModel<Unit, ManageWalletEvent>(Unit) {

    init {
        pinCodeCommunicator.onSuccessPassCode = {
            when (it) {
                ReasonEnterCode.SHOW_DETAILS -> {
                    setEvent(ManageWalletEvent.ShowData(true))
                }

                else -> Unit
            }
        }
    }

    fun handleEvent(event: ManageWalletEvent) {
        setEvent(event)
    }

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
