package com.android.greenapp.presentation.main.importtoken

import androidx.lifecycle.ViewModel
import com.android.greenapp.domain.interact.BlockChainInteract
import com.android.greenapp.domain.interact.TokenInteract
import com.android.greenapp.domain.interact.WalletInteract
import javax.inject.Inject

/**
 * Created by bekjan on 12.07.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class ImportTokenViewModel @Inject constructor(
    private val tokenInteract: TokenInteract,
    private val blockChainInteract: BlockChainInteract,
    private val walletInteract: WalletInteract
) :
    ViewModel() {

    suspend fun importToken(fingerPrint: Long, hash: String, added: Boolean) =
        walletInteract.importToken(fingerPrint, hash, added)

    suspend fun getTokenListAndSearch(fingerPrint: Long, nameCode:String?) =
        tokenInteract.getTokenListAndSearch(fingerPrint, nameCode)


}