package com.android.greenapp.domain.interact

import com.android.greenapp.data.network.BlockChainService
import com.android.greenapp.data.network.dto.blockchain.MnemonicDto
import com.android.greenapp.domain.entity.Wallet
import com.android.greenapp.presentation.tools.Resource

/**
 * Created by bekjan on 06.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */
interface BlockChainInteract {


    suspend fun saveNewWallet(
        wallet: Wallet
    ):Resource<String>

    suspend fun updateBalanceAndTransactionsPeriodically()

    suspend fun push_tx(
        jsonSpendBundle:String,
        url: String
    ):Resource<String>


}