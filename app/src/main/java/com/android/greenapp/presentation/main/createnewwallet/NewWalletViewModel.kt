package com.android.greenapp.presentation.main.createnewwallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.greenapp.data.network.dto.blockchain.MnemonicDto
import com.android.greenapp.domain.entity.Wallet
import com.android.greenapp.domain.interact.BlockChainInteract
import com.android.greenapp.domain.interact.GreenAppInteract
import com.android.greenapp.presentation.tools.Resource
import com.example.common.tools.VLog
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
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

    private val _publicKeyAndPrivateKey =
        MutableStateFlow<Resource<String>>(Resource.loading())
    val publicKeyAndPrivateKey: StateFlow<Resource<String>> =
        _publicKeyAndPrivateKey.asStateFlow()
    private var job: Job? = null

    private val _generateMnemonic =
        MutableSharedFlow<Resource<MnemonicDto>>(1, 1)
    val generateMnemonic: SharedFlow<Resource<MnemonicDto>> =
        _generateMnemonic

    fun createNewWallet(
        wallet: Wallet
    ) = viewModelScope.launch {
        blockChainInteract.saveNewWallet(
            wallet
        )
    }

    fun getGeneratedMnemonics() {
        job?.cancel()
        job = viewModelScope.launch {
            try {
                _generateMnemonic.emit(Resource.loading())
                val res = blockChainInteract.getGenerateMnemonics()
                _generateMnemonic.emit(res)
            } catch (ex: Exception) {
                VLog.d("GenerateMnemonics Exception : ${ex.message}")
                _generateMnemonic.emit(Resource.error(ex))
            }
        }
    }

    suspend fun getCoinDetails(coinCode: String) = greenAppInteract.getCoinDetails(coinCode)


}