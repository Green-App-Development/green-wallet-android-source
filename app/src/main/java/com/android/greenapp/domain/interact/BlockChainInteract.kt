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

    suspend fun getGenerateMnemonics(): Resource<MnemonicDto>

    suspend fun saveNewWallet(
        wallet: Wallet
    )

    suspend fun getAllPublicKeys(): Resource<List<Long>>

    suspend fun requestBalanceEachWallets()

    suspend fun requestAllTransactions()

    suspend fun sendTransaction(
        amount: Double,
        fee: Double,
        address: String,
        fingerPrint: Long,
        networkType: String,
        walletId: Int
    ): Resource<String>


    suspend fun logIn(
        fingerPrint: Long,
        curBlockChainService: BlockChainService
    ): Resource<String>

    suspend fun updateTokensChiaNetwork()

    suspend fun getWalletTokensForChiaNetwork(
        fingerPrint: Long,
        curBlockChainService: BlockChainService
    )

    suspend fun deleteAllKeys()

    suspend fun updateBalanceAndTransactionsPeriodically()

    suspend fun push_tx(
        jsonSpendBundle:String,
        url: String
    ):Resource<String>


}