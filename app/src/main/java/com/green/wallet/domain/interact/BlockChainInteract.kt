package com.green.wallet.domain.interact

import com.green.wallet.data.local.entity.WalletEntity
import com.green.wallet.domain.domainmodel.NFTInfo
import com.green.wallet.domain.domainmodel.NftOfferCoin
import com.green.wallet.domain.domainmodel.PushResult
import com.green.wallet.domain.domainmodel.Wallet
import com.green.wallet.presentation.tools.Resource

interface BlockChainInteract {


    suspend fun saveNewWallet(
        wallet: Wallet,
        imported: Boolean
    ): Resource<String>

    suspend fun updateBalanceAndTransactionsPeriodically()

    suspend fun push_tx(
        jsonSpendBundle: String,
        url: String,
        sendAmount: Double,
        networkType: String,
        fingerPrint: Long,
        code: String,
        destPuzzleHash: String,
        address: String,
        fee: Double,
        spentCoinsJson: String,
        spentCoinsToken: String
    ): Resource<PushResult>

    suspend fun updateTokenBalanceWithFullNode(wallet: WalletEntity)

    suspend fun updateWalletBalanceWithTransactions(wallet: WalletEntity)

    suspend fun push_tx_nft(
        jsonSpendBundle: String,
        url: String,
        destPuzzleHash: String,
        nftInfo: NFTInfo,
        spentCoinsJson: String,
        fee: Double,
        confirmHeight: Int,
        networkType: String
    ): Resource<String>

    suspend fun getNftInfoByCoinID(
        networkType: String,
        coinID: String
    ): NftOfferCoin?

}
