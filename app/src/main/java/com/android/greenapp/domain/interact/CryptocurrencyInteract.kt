package com.android.greenapp.domain.interact

import com.android.greenapp.domain.domainmodel.CurrencyItem
import kotlinx.coroutines.flow.Flow

/**
 * Created by bekjan on 20.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */
interface CryptocurrencyInteract {

    suspend fun updateCourseCryptoInDb()

    suspend fun getCurrentCurrencyCourseByNetwork(type: String): Flow<CurrencyItem>

    suspend fun getAllTails()

    suspend fun getCourseCurrencyCoin(code: String): Double


}
