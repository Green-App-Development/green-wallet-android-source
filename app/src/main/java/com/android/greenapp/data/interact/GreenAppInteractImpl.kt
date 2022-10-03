package com.android.greenapp.data.interact

import com.android.greenapp.data.local.Converters
import com.android.greenapp.data.local.NotifOtherDao
import com.android.greenapp.data.local.TokenDao
import com.android.greenapp.data.local.entity.NotifOtherEntity
import com.android.greenapp.data.network.GreenAppService
import com.android.greenapp.data.network.dto.greenapp.language.LanguageItem
import com.android.greenapp.data.network.dto.greenapp.language.LanguageResponse
import com.android.greenapp.data.network.dto.greenapp.network.NetworkItem
import com.android.greenapp.data.preference.PrefsManager
import com.android.greenapp.domain.entity.CoinDetails
import com.android.greenapp.domain.interact.GreenAppInteract
import com.android.greenapp.domain.interact.PrefsInteract
import com.android.greenapp.presentation.custom.NotificationHelper
import com.android.greenapp.presentation.custom.getPreferenceKeyForCoinDetail
import com.android.greenapp.presentation.custom.getPreferenceKeyForNetworkItem
import com.android.greenapp.presentation.custom.isExceptionBelongsToNoInternet
import com.android.greenapp.presentation.tools.JsonHelper
import com.android.greenapp.presentation.tools.Resource
import com.android.greenapp.presentation.tools.SIX_HOURS_IN_MILLIS_SECONDS
import com.example.common.tools.VLog
import com.example.common.tools.convertDateFormatToMilliSeconds
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.b3nedikt.restring.Restring
import kotlinx.coroutines.delay
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.TimeoutException
import javax.inject.Inject

/**
 * Created by bekjan on 06.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class GreenAppInteractImpl @Inject constructor(
	private val greenAppService: GreenAppService,
	private val prefs: PrefsInteract,
	private val gson: Gson,
	private val jsonHelper: JsonHelper,
	private val notifOtherDao: NotifOtherDao,
	private val tokenDao: TokenDao,
	private val notifHelper: NotificationHelper
) :
	GreenAppInteract {

	override suspend fun getAvailableLanguageList(): Resource<List<LanguageItem>> {
		try {
			val response = greenAppService.getLanguageList()
			if (response.isSuccessful) {
				VLog.d("LanguageResponse : $response")
				val baseResponse = response.body()
				if (baseResponse != null) {
					if (baseResponse.success) {
						val langResponse = gson.fromJson(
							baseResponse.result.toString(),
							LanguageResponse::class.java
						)
						VLog.d("Got Lang List : ${langResponse.list}")
						return Resource.success(
							langResponse.list
						)
					} else {
						VLog.d("BaseResponse is not successful")
					}
				} else {
					VLog.d("BaseResponse is null")
				}
			} else {
				VLog.d("Response is not successful")
			}
		} catch (ex: java.lang.Exception) {
			VLog.d("Exception in downloading lang list : ${ex.message}")
			return Resource.error(ex)
		}
		return Resource.error(Exception("Unknown result in downloading language list"))
	}

	override suspend fun downloadLanguageTranslate(langCode: String): Resource<String> {
		try {
			val response = greenAppService.getLanguageTranslate(
				langCode,
				prefs.getSettingString(PrefsManager.VERSION_REQUEST, "")
			)
			val resMap = convertResponseToHashMap(response)
			val chooseLocale = Locale(langCode)
			Restring.putStrings(chooseLocale, resMap)
			Restring.locale = chooseLocale
			VLog.d("Downloading Language Resource From Server code : $langCode")
			prefs.saveSettingString(PrefsManager.CUR_LANGUAGE_CODE, langCode)
			prefs.saveSettingString(
				PrefsManager.LANGUAGE_RESOURCE,
				Converters.hashMapToString(resMap)
			)
			response.close()
			return Resource.success("OK")
		} catch (ex: java.lang.Exception) {
			VLog.d("Failed getting language string resource and saving it in db : ${ex.message}")
			return Resource.error(ex)
		}
	}

	override suspend fun changeLanguageIsSavedBefore() {
		val resLanguageResource =
			prefs.getSettingString(PrefsManager.LANGUAGE_RESOURCE, "")
		if (resLanguageResource.isEmpty()) return
		val resMap = Converters.stringToHashMap(resLanguageResource)
		val langCode = prefs.getSettingString(PrefsManager.CUR_LANGUAGE_CODE, "")
		val chooseLocale = Locale(langCode)
		Restring.putStrings(chooseLocale, resMap)
		Restring.locale = chooseLocale
	}

	override suspend fun getAvailableNetworkItemsFromRestAndSave() {
		try {
			val res = greenAppService.getAvailableBlockChains()
			if (res.isSuccessful) {
				val baseResponse = res.body()!!
				if (baseResponse.success) {
					val networkItemList = baseResponse.result.list
					saveNetworkItemsToPrefs(baseResponse.result.list)
					val jsonAllNetworkItems = gson.toJson(networkItemList)
					VLog.d("Saving AllJsonNetworkItemsList to prefs : $jsonAllNetworkItems")
					prefs.saveObjectString(PrefsManager.ALL_NETWORK_ITEMS_LIST, jsonAllNetworkItems)

				} else {
					VLog.d("Base Response is not success")
				}

			} else {
				VLog.d("NetworkItems Response is not success")
			}

		} catch (ex: java.lang.Exception) {
			VLog.d("Exception in getting network items  : $ex")
		}
	}

	override suspend fun getAllNetworkItemsListFromPrefs(): Resource<List<NetworkItem>> {
		val jsonAllNetworkItemList = prefs.getObjectString(PrefsManager.ALL_NETWORK_ITEMS_LIST)
		if (jsonAllNetworkItemList.isNotEmpty()) {
			val type = object : TypeToken<MutableList<NetworkItem>>() {}.type
			val convertedNetworkItems =
				gson.fromJson<List<NetworkItem>>(jsonAllNetworkItemList, type)
			return Resource.success(convertedNetworkItems)

		}
		return Resource.error(Exception("AllNetworkItemList is empty"))
	}

	override suspend fun requestOtherNotifItems() {
		try {

			val res =
				greenAppService.getNotifOtherItems(
					prefs.getSettingString(
						PrefsManager.CUR_LANGUAGE_CODE,
						"ru"
					)
				)

			if (res.isSuccessful) {

				val otherNotifItemsJsonArray = JSONArray(
					res.body()!!.getAsJsonObject("result").getAsJsonArray("list").toString()
				)
				for (at in 0 until otherNotifItemsJsonArray.length()) {
					val curJsonObject = otherNotifItemsJsonArray.getJSONObject(at)

					val guid = curJsonObject.getString("guid")
					val timeStamp =
						convertDateFormatToMilliSeconds(curJsonObject.getString("created_at")) + SIX_HOURS_IN_MILLIS_SECONDS
					val message = curJsonObject.getString("message")
					val notifOther = NotifOtherEntity(guid, timeStamp, message)

					val existInDb = notifOtherDao.getNotifOtherItemByGuid(notifOther.guid)
					if (!existInDb.isPresent && timeStamp > prefs.getSettingLong(
							PrefsManager.APP_START_TIME,
							System.currentTimeMillis()
						)
					) {
						notifHelper.callGreenAppNotificationMessages(
							notifOther.message,
							notifOther.created_at_time
						)
						notifOtherDao.insertingNotifOther(notifOther)
					} else {
						VLog.d("Not insert OtherNotifItems : $notifOther")
					}

				}
			} else {
				VLog.d("Response is not success in requestingOtherNotifItems ${res.body()} ")
			}

		} catch (ex: Exception) {
			VLog.d("Exception occurred in requestingOtherNotifItems $ex ")
		}
	}

	override suspend fun getAgreementsText(): Resource<String> {
		try {
			val code = prefs.getSettingString(PrefsManager.CUR_LANGUAGE_CODE, "ru")
			val res = greenAppService.getAgreementText(code)

			if (res.isSuccessful) {

				val agreementText = JSONObject(
					res.body()!!.getAsJsonObject("result").toString()
				).getString("agreement_text")
				return Resource.success(agreementText)
			} else {
				VLog.d("Response is not successful : ${res.body()}")
			}

		} catch (ex: Exception) {
			VLog.d("Exception in gettingAgreementText : $ex")
			return Resource.error(ex)
		}
		return Resource.error(Exception("Unknown error in gettingAgreement text"))
	}


	override suspend fun updateCoinDetails() {
		try {

			val res =
				greenAppService.getCoinDetails(
					prefs.getSettingString(
						PrefsManager.CUR_LANGUAGE_CODE,
						"ru"
					)
				)
			if (res.isSuccessful) {

				val coinList = res.body()!!.getAsJsonObject("result").getAsJsonArray("list")


				for (coinJsonElement in coinList) {
					val jsonObject = JSONObject(coinJsonElement.toString())
					val coinCode = jsonObject.getString("code")
					//XCH XCC
					val blockChainName = jsonObject.getString("blockchain_name")
					val name = jsonObject.getString("name")
					val codeCoin = jsonObject.getString("code")
					val description = jsonObject.getString("description")
					val characteristics = jsonObject.getString("specification")
					val logo_url = jsonObject.getString("logo_url")
					val fee_commission =
						jsonObject.getString("fee_transaction").replace(',', '.').toDouble()
					val coinDetails = CoinDetails(
						blockChainName,
						name,
						codeCoin,
						description,
						characteristics,
						fee_commission
					)
					VLog.d("Updating coin details from the server : $coinDetails")
					prefs.saveObjectString(
						getPreferenceKeyForCoinDetail(codeCoin),
						gson.toJson(coinDetails)
					)
				}
			} else {
				VLog.d("Response is not success in getting coins details list : ${res.body()}")
			}

		} catch (ex: Exception) {
			VLog.d("Exception in getting coins details  -> ${ex.message}")
		}
	}

	override suspend fun getCoinDetails(code: String): CoinDetails {
		val coinDetailsJson = prefs.getObjectString(getPreferenceKeyForCoinDetail(code))
		val coin = gson.fromJson(coinDetailsJson, CoinDetails::class.java)
		return coin
	}


	private suspend fun saveNetworkItemsToPrefs(items: List<NetworkItem>) {
		for (network in items) {
			val jsonObjStr = gson.toJson(network)
			prefs.saveObjectString(getPreferenceKeyForNetworkItem(network.name), jsonObjStr)
		}
	}

	private fun convertResponseToHashMap(response: ResponseBody): HashMap<String, String> {
		val resHashMap = hashMapOf<String, String>()
		try {
			jsonHelper.getFlattenedHashmapFromJsonForLocalization(
				"",
				ObjectMapper().readTree(response.string()),
				resHashMap
			)
		} catch (ex: Exception) {
			VLog.d("Exception : ${ex.message}")
		}
		return resHashMap
	}


}
