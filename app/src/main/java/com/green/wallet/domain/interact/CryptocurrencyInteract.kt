package com.green.wallet.domain.interact

import com.green.wallet.domain.domainmodel.CurrencyItem
import kotlinx.coroutines.flow.Flow


interface CryptocurrencyInteract {

    suspend fun updateCourseCryptoInDb()

    suspend fun getCurrentCurrencyCourseByNetwork(type: String): Flow<CurrencyItem>

    suspend fun getAllTails()

    suspend fun getCourseCurrencyCoin(code: String): Double

    suspend fun updateTokensPrice()

    suspend fun checkingDefaultWalletTails()



}
