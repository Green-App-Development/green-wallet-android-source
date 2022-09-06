package com.android.greenapp.data.interact

import com.android.greenapp.data.local.TokenDao
import com.android.greenapp.data.network.CryptocurrencyService
import com.android.greenapp.data.network.GreenAppService
import com.android.greenapp.domain.entity.CurrencyItem
import com.android.greenapp.domain.interact.CryptocurrencyInteract
import com.android.greenapp.domain.interact.PrefsInteract
import com.android.greenapp.presentation.custom.getPreferenceKeyForCurNetworkPrev24ChangeDouble
import com.android.greenapp.presentation.custom.getPreferenceKeyForCurStockNetworkDouble
import com.example.common.tools.*
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import javax.inject.Inject

/**
 * Created by bekjan on 20.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class CryptocurrencyImpl @Inject constructor(
    private val cryptocurrencyService: CryptocurrencyService,
    private val prefs: PrefsInteract,
    private val gson: Gson,
    private val greenAppService: GreenAppService,
    private val tokenDao: TokenDao
) :
    CryptocurrencyInteract {

    override suspend fun updateCourseCryptoInDb() {
        try {
//            updateChiaCourse()
//            updateChivesCourse()
            updateChiaChivesCourse()
            updateTokensPrice()
        } catch (ex: Exception) {
            VLog.d("Exception in getting cryptoCurrency  ${ex.message}")
        }
    }

    private suspend fun updateChiaChivesCourse() {
        try {
            val res = greenAppService.getUpdatedChiaChivesCourse(com.android.greenapp.presentation.tools.URL_CHIA_CHIVES)
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
            } else {
                VLog.d("Result is not success in updating chia and chives course")
            }
        } catch (ex: Exception) {
            VLog.d("Exception in updating chia, chives : $ex")
        }
    }

    private suspend fun updateTokensPrice() {
        try {
            val res = greenAppService.getAllTailsPrice()
            if (res.isSuccessful) {
                val tailPricesList =
                    res.body()!!.asJsonObject.getAsJsonObject("result").getAsJsonArray("list")
                for (tokenJson in tailPricesList) {
                    val tokenCode = tokenJson.asJsonObject["code"].asString
                    val tokenPrice = tokenJson.asJsonObject["price"].asDouble
                    tokenDao.updateTokenPrice(tokenPrice, tokenCode)
                }

            } else {
                VLog.d("Response is not successful getting tail prices: ${res.body()}")
            }

        } catch (ex: Exception) {
            VLog.d("Exception in getting token price from greenAppService  : $ex")
        }
    }

    private suspend fun updateChivesCourse() {
        val res = cryptocurrencyService.getLatestCurrency(com.android.greenapp.presentation.tools.BASE_URL_CRYPTO_CHIVES)
        if (res.isSuccessful) {

            val jsonArrayCourse =
                res.body()!!.getAsJsonArray("data")

            for (jsonObject in jsonArrayCourse) {

                if (jsonObject.asJsonObject.get("symbol").toString() == "\"xcc_usdt\"") {
                    val chivesCourse = JSONObject(
                        jsonObject.asJsonObject.get("ticker").toString()
                    ).getDouble("latest")
                    VLog.d(
                        "Saving XCC_USDT course : $chivesCourse : ${
                            jsonObject.asJsonObject.get("symbol")
                        }"
                    )
                    prefs.saveCoursePriceDouble(
                        getPreferenceKeyForCurStockNetworkDouble("Chives"),
                        chivesCourse
                    )
                }
            }

        } else {
            VLog.d("Request is not successful  : ${res.message()}")
        }
    }


    private suspend fun updateChiaCourse() {
        val response = cryptocurrencyService.getLatestCurrency(com.android.greenapp.presentation.tools.BASE_URL_CRYPTO_CHIA)
        if (response.isSuccessful) {
            val courseJsonList = response.body()?.getAsJsonArray("data")
            if (courseJsonList != null) {
                for (course in courseJsonList) {
                    val symbol = course.asJsonObject.get("symbol").asString
                    if (symbol == "xchusdt") {
                        val bid = course.asJsonObject.get("bid").asDouble
                        VLog.d("Saving xchusdt : $bid")
                        prefs.saveCoursePriceDouble(
                            getPreferenceKeyForCurStockNetworkDouble("Chia"),
                            bid
                        )
                    }
                }
            } else {
                VLog.d("CourseJsonList is null")
            }
        } else {
            VLog.d("Response is not successful  : ${response.message()}")
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
        try {
            val res = greenAppService.getAllTails()
            if (res.isSuccessful) {

                val baseResSuccess = res.body()!!.success
                if (baseResSuccess) {

                    val tokenList = res.body()!!.result.list.map { it.toTokenEntity() }
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

    override suspend fun getCourseCurrencyCoin(networkType: String): Double {
        val curCoinPrice =
            prefs.getCoursePriceDouble(
                getPreferenceKeyForCurStockNetworkDouble(networkType.split(" ")[0]),
                0.0
            )
        return curCoinPrice
    }


}