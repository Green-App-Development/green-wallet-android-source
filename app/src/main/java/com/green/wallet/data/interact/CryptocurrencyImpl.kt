package com.green.wallet.data.interact

import android.content.Context
import com.green.wallet.data.local.TokenDao
import com.green.wallet.data.local.WalletDao
import com.green.wallet.data.local.entity.TokenEntity
import com.green.wallet.data.network.GreenAppService
import com.green.wallet.data.network.dto.greenapp.network.NetworkItem
import com.green.wallet.data.network.dto.greenapp.token.TokenDto
import com.green.wallet.data.preference.PrefsManager.Companion.ALL_NETWORK_ITEMS_LIST
import com.green.wallet.domain.domainmodel.CurrencyItem
import com.green.wallet.domain.interact.CryptocurrencyInteract
import com.green.wallet.domain.interact.PrefsInteract
import com.green.wallet.presentation.App
import com.green.wallet.presentation.custom.convertListToStringWithSpace
import com.green.wallet.presentation.custom.getPreferenceKeyForCurNetworkPrev24ChangeDouble
import com.green.wallet.presentation.custom.getPreferenceKeyForCurStockNetworkDouble
import com.green.wallet.presentation.tools.METHOD_CHANNEL_GENERATE_HASH
import com.example.common.tools.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.green.wallet.presentation.tools.URL_CHIA_CHIVES
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.*
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
    private val tokenDao: TokenDao,
    private val context: Context,
    private val walletDao: WalletDao
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
                greenAppService.getUpdatedChiaChivesCourse(URL_CHIA_CHIVES)
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
                var chia24Hour = 0.0
                if (!JSONObject(chia.toString()).isNull("usd_24h_change")) {
                    chia24Hour = JSONObject(chia.toString()).getDouble("usd_24h_change")
                }
                var chives24Hour = 0.0
                if (!JSONObject(chives.toString()).isNull("usd_24h_change")) {
                    chives24Hour = JSONObject(chia.toString()).getDouble("usd_24h_change")
                }
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

    override suspend fun updateTokensPrice() {
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

    override suspend fun checkingDefaultWalletTails() {
        try {

            if (!(context.applicationContext as App).isFlutterEngineInitialized())
                return

            val defaultTails = tokenDao.getTokensDefaultOnScreen()
            VLog.d("Got default tails : $defaultTails")
            val chiaWallets = walletDao.getWalletByNetworkTypeChia()
            val methodChannel = MethodChannel(
                (context.applicationContext as App).flutterEngine.dartExecutor.binaryMessenger,
                METHOD_CHANNEL_GENERATE_HASH
            )
            for (wallet in chiaWallets) {
                val hashListImported = wallet.hashListImported
                VLog.d("HashListImported before : $hashListImported")
                var needToWait = false
                defaultTails.forEach { token ->
                    if (!hashListImported.containsKey(token.hash)) {
                        needToWait = true
                        val map = hashMapOf<String, String>()
                        //temporary
                        map["puzzle_hashes"] = convertListToStringWithSpace(wallet.puzzle_hashes)
                        map["asset_id"] = token.hash
                        withContext(Dispatchers.Main) {
                            methodChannel.setMethodCallHandler { method, calLBack ->
                                if (method.method == "generate_outer_hash") {
                                    val args = method.arguments as HashMap<*, *>
                                    val outer_hashes = args[token.hash] as List<String>
                                    hashListImported[token.hash] = outer_hashes
                                }
                            }
                            methodChannel.invokeMethod("generatewrappedcatpuzzle", map)
                        }
                    }
                    delay(500)
                    VLog.d("HashListImported after : $hashListImported")
                    walletDao.updateChiaNetworkHashListImportedByAddress(
                        wallet.address,
                        hashListImported
                    )
                }
            }

        } catch (ex: Exception) {
            VLog.d("Exception in checking default wallet tails : ${ex.message}")
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

        val exChiaToken = tokenDao.getTokenByCode("XCH")
        var exChiaPrice = 0.0
        if (exChiaToken.isPresent)
            exChiaPrice = exChiaToken.get().price
        val exChivesToken = tokenDao.getTokenByCode("XCC")
        var exChivesPrice = 0.0
        if (exChivesToken.isPresent)
            exChivesPrice = exChivesToken.get().price
        val chiaTokenEntity = TokenEntity("XCH", "Chia", "", "", exChiaPrice, 0, true)
        tokenDao.insertToken(chiaTokenEntity)
        val chivesTokenEntity = TokenEntity("XCC", "Chives", "", "", exChivesPrice, 0, true)
        tokenDao.insertToken(chivesTokenEntity)

        //make all tails enabled false
        val existingTails = tokenDao.getTokenListAndSearch(null)
            .map { TokenDto(it.name, it.code, it.hash, it.logo_url, 0, "") }
            .toMutableList()

        val jsonAllNetworkItemList = prefs.getObjectString(ALL_NETWORK_ITEMS_LIST)
        val type = object : TypeToken<MutableList<NetworkItem>>() {}.type
        val chiaName =
            gson.fromJson<List<NetworkItem>>(jsonAllNetworkItemList, type)[0].name
        try {
            val res = greenAppService.getAllTails(chiaName)
            if (res.isSuccessful) {
                val baseResSuccess = res.body()!!.success
                if (baseResSuccess) {
//					VLog.d("Received all tails inf on crypto: ${res.body()!!.result}")
                    val tokenListRes = res.body()!!.result.list.map { it.toTokenEntity() }
//					VLog.d("TokenList on  : $tokenListRes")
                    for (tokenEntity in tokenListRes) {
                        tokenDao.insertToken(tokenEntity)
                        val tokenDto = TokenDto(
                            tokenEntity.name,
                            tokenEntity.code,
                            tokenEntity.hash,
                            tokenEntity.logo_url,
                            0,
                            ""
                        )
                        existingTails.remove(tokenDto)
                    }
                    VLog.d("Existing tails should be disabled : $existingTails")
                    existingTails.forEach {
                        tokenDao.updateTokenEnable(it.code, false)
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
