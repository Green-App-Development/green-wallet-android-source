package com.green.wallet.domain.interact

import com.green.wallet.domain.domainmodel.SpentCoin
import kotlinx.coroutines.flow.Flow

interface SpentCoinsInteract {

    suspend fun insertSpentCoinsJson(
        spentCoinsJson: String,
        timeCreated: Long,
        code: String,
        fkAddress: String
    )

    suspend fun getSpentCoinsToPushTrans(
        networkType: String,
        address: String,
        tokenCode: String
    ): List<SpentCoin>

    fun getSumSpentCoinsForSpendableBalance(
        networkType: String,
        address: String,
        tokenCode: String
    ): Flow<DoubleArray>

    suspend fun getSpendableBalanceByTokenCode(
        assetID: String,
        tokenCode: String,
        address: String
    ): Flow<Double>

    suspend fun getSpentCoinsByTransactionTime(timeCreated: Long): List<SpentCoin>

    suspend fun getSpentCoinsBalanceByAddressAndCode(address: String, code: String): Flow<Double>

}
