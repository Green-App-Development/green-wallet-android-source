package com.android.greenapp.domain.interact

import com.android.greenapp.domain.domainmodel.SpentCoin

interface SpentCoinsInteract {

    suspend fun insertSpentCoinsJson(
        spentCoinsJson: String,
        time_created: Long,
        code: String,
        fk_address: String
    )

    suspend fun getSpentCoinsToPushTrans(
        networkType: String,
        address: String,
        tokenCode: String
    ): List<SpentCoin>

    suspend fun getSumSpentCoinsForSpendableBalance(networkType:String,address: String, tokenCode: String): DoubleArray


}