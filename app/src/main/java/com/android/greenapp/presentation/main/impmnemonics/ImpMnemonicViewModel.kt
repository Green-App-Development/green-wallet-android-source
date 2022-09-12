package com.android.greenapp.presentation.main.impmnemonics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.greenapp.domain.entity.Wallet
import com.android.greenapp.domain.interact.BlockChainInteract
import com.android.greenapp.domain.interact.PrefsInteract
import com.android.greenapp.domain.interact.WalletInteract
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
    private val walletInteract: WalletInteract
) : ViewModel() {

    private val _publicKeyAndPrivateKey =
        MutableStateFlow<Resource<String>>(Resource.loading())
    val publicKeyAndPrivateKey: StateFlow<Resource<String>> =
        _publicKeyAndPrivateKey.asStateFlow()
    private var job: Job? = null


    fun importNewWallet(wallet: Wallet, callBack: () -> Unit) {
        viewModelScope.launch {
            blockChainInteract.saveNewWallet(wallet)
            callBack()
        }
    }

    suspend fun checkIfMnemonicsExist(mnemonics: List<String>, networkType: String) =
        walletInteract.checkIfMnemonicsExistInDB(mnemonics, networkType)

}