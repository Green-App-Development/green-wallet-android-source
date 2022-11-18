package com.android.greenapp.data.interact

import com.android.greenapp.data.local.TokenDao
import com.android.greenapp.data.local.entity.TokenEntity
import com.android.greenapp.data.network.GreenAppService
import com.android.greenapp.data.network.dto.greenapp.network.NetworkItem
import com.android.greenapp.data.preference.PrefsManager.Companion.ALL_NETWORK_ITEMS_LIST
import com.android.greenapp.domain.domainmodel.CurrencyItem
import com.android.greenapp.domain.interact.CryptocurrencyInteract
import com.android.greenapp.domain.interact.PrefsInteract
import com.android.greenapp.presentation.custom.getPreferenceKeyForCurNetworkPrev24ChangeDouble
import com.android.greenapp.presentation.custom.getPreferenceKeyForCurStockNetworkDouble
import com.example.common.tools.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import javax.inject.Inject

/**
 * Created by bekjan on 20.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class CryptocurrencyImpl @Inject constructor(
    private val prefs: PrefsInteract,
    private val gson: Gson,
    private val greenAppService: GreenAppService,
    private val tokenDao: TokenDao
) :
    CryptocurrencyInteract {

    override suspend fun updateCourseCryptoInDb() {
        try {
            updateChiaChivesCourse()
            updateTokensPrice()
        } catch (ex: Exception) {
            VLog.d("Exception in getting cryptoCurrency  ${ex.message}")
        }
    }

    private suspend fun updateChiaChivesCourse() {
        try {
            val res =
                greenAppService.getUpdatedChiaChivesCourse(com.android.greenapp.presentation.tools.URL_CHIA_CHIVES)
            if (res.isSuccessful) {

                val chia = res.body()!!.asJsonObject["chia"]
                val chives = res.body()!!.asJsonObject["chives-coin"]
                val chiaPrice = JSONObject(chia.toString()).getDouble("usd")
                val chivesPrice = JSONObject(chives.toString()).getDouble("usd")
                VLog.d("Saving Chia and Chives course Exchange : $chiaPrice, ${chivesPrice}")
                prefs.saveCoursePriceDouble(
                    getPreferenceKeyForCurStockNetworkDouble("Chia"),
                    chiaPrice
                )
                prefs.saveCoursePriceDouble(
                    getPreferenceKeyForCurStockNetworkDouble("Chives"),
                    chivesPrice
                )
                val chia24Hour = JSONObject(chia.toString()).getDouble("usd_24h_change")
                val chives24Hour = JSONObject(chives.toString()).getDouble("usd_24h_change")
                prefs.saveCoursePriceDouble(
                    getPreferenceKeyForCurNetworkPrev24ChangeDouble("Chia"),
                    chia24Hour
                )
                prefs.saveCoursePriceDouble(
                    getPreferenceKeyForCurNetworkPrev24ChangeDouble("Chives"),
                    chives24Hour
                )
                tokenDao.updateTokenPrice(chiaPrice, "XCH")
                tokenDao.updateTokenPrice(chivesPrice, "XCC")
            } else {
                VLog.d("Result is not success in updating chia and chives course")
            }
        } catch (ex: Exception) {
            VLog.d("Exception in updating chia, chives : $ex")
        }
    }

    override  suspend fun updateTokensPrice() {
        try {
            val res = greenAppService.getAllTailsPrice()
            if (res.isSuccessful) {
                val tailPricesList =
                    res.body()!!.asJsonObject.getAsJsonObject("result").getAsJsonArray("list")
                for (tokenJson in tailPricesList) {
                    val tokenCode = tokenJson.asJsonObject["code"].asString
                    var tokenPrice = 0.0
                    if (!JSONObject(tokenJson.toString()).isNull("price")) {
                        tokenPrice =
                            tokenJson.asJsonObject["price"].asString.toDoubleOrNull() ?: 0.0
                    } else {
                        VLog.d("Token property is null ")
                    }
                    tokenDao.updateTokenPrice(tokenPrice, tokenCode)
                }

            } else {
                VLog.d("Response is not successful getting tail prices: ${res.body()}")
            }

        } catch (ex: Exception) {
            VLog.d("Exception in getting token price from greenAppService  : $ex")
        }
    }

    override suspend fun getCurrentCurrencyCourseByNetwork(type: String): Flow<CurrencyItem> {
        return prefs.getDoubleFlow(
            getPreferenceKeyForCurStockNetworkDouble(type.split(" ")[0]),
            0.0
        )
            .map {
                val prev24HourChange = prefs.getCoursePriceDouble(
                    getPreferenceKeyForCurNetworkPrev24ChangeDouble(type.split(" ")[0]), 0.0
                )
                return@map CurrencyItem(
                    it,
                    formatPercentTwoDecimalPrecision(Math.abs(prev24HourChange)),
                    prev24HourChange >= 0
                )
            }
    }


    private fun formatPercentTwoDecimalPrecision(value: Double): String {
        return String.format("%.2f", value).replace(",", ".")
    }


    override suspend fun getAllTails() {
        val chiaTokenEntity = TokenEntity("XCH", "Chia", "", "", 0.0, 0, true)
        tokenDao.insertToken(chiaTokenEntity)
        val chivesTokenEntity = TokenEntity("XCC", "Chives", "", "", 0.0, 0, true)
        tokenDao.insertToken(chivesTokenEntity)

        //make all tails enabled false
        val existingTails = tokenDao.getTokenListAndSearch(null)
        for (tail in existingTails) {
            tail.enabled = false
            tokenDao.insertToken(tail)
        }

        val jsonAllNetworkItemList = prefs.getObjectString(ALL_NETWORK_ITEMS_LIST)
        val type = object : TypeToken<MutableList<NetworkItem>>() {}.type
        val chiaName =
            gson.fromJson<List<NetworkItem>>(jsonAllNetworkItemList, type)[0].name
        try {
            val res = greenAppService.getAllTails(chiaName)
            if (res.isSuccessful) {

                val baseResSuccess = res.body()!!.success
                if (baseResSuccess) {
                    VLog.d("Received all tails inf on crypto: ${res.body()!!.result}")
                    val tokenList = res.body()!!.result.list.map { it.toTokenEntity() }
                    VLog.d("TokenList on  : $tokenList")
                    tokenList.forEach {
                        tokenDao.insertToken(it)
                    }
                } else {
                    VLog.d("Base Response is not successful getting tails: ${res.body()}")
                }

            } else {
                VLog.d("Response is not successful getting tails: ${res.body()}")
            }
        } catch (ex: Exception) {
            VLog.d("Exception in getting tail list  : $ex")
        }
    }

    override suspend fun getCourseCurrencyCoin(code: String): Double {
        val optionalToken = tokenDao.getTokenByCode(code)
        if (optionalToken.isPresent)
            return optionalToken.get().price
        return 0.0
    }


}
