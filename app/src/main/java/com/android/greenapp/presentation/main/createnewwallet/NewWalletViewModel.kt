package com.android.greenapp.presentation.main.createnewwallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.greenapp.domain.entity.Wallet
import com.android.greenapp.domain.interact.BlockChainInteract
import com.android.greenapp.domain.interact.GreenAppInteract
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by bekjan on 07.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */

class NewWalletViewModel @Inject constructor(
    private val blockChainInteract: BlockChainInteract,
    private val greenAppInteract: GreenAppInteract
) :
    ViewModel() {

    fun createNewWallet(
        wallet: Wallet,
        callBack: () -> Unit
    ) = viewModelScope.launch {
        blockChainInteract.saveNewWallet(
            wallet=wallet,
           imported= false
        )
        callBack()
    }

    suspend fun getCoinDetails(coinCode: String) = greenAppInteract.getCoinDetails(coinCode)

}
