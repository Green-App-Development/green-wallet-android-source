package com.green.wallet.presentation.main.receive

import androidx.lifecycle.ViewModel
import com.green.wallet.domain.interact.WalletInteract
import javax.inject.Inject


class ReceiveViewModel @Inject constructor(private val walletInteract: WalletInteract) :
    ViewModel() {


    suspend fun getWalletListHomeIsAddedByNetworkType(
        networkType: String,
        fingerPrint: Long?
    ) =
        walletInteract.getWalletListByNetworkTypeFingerPrint(networkType = networkType, fingerPrint)


    suspend fun getDistinctNetworkTypeValues() = walletInteract.getDistinctNetworkTypes()


}
