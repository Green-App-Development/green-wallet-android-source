package com.android.greenapp.data.interact

import android.os.Build
import androidx.annotation.RequiresApi
import com.android.greenapp.data.network.GreenAppService
import com.android.greenapp.data.network.dto.support.ListingPost
import com.android.greenapp.data.network.dto.support.QuestionPost
import com.android.greenapp.data.preference.PrefsManager
import com.android.greenapp.domain.domainmodel.FAQItem
import com.android.greenapp.domain.interact.PrefsInteract
import com.android.greenapp.domain.interact.SupportInteract
import com.android.greenapp.presentation.custom.parseException
import com.android.greenapp.presentation.tools.Resource
import com.example.common.tools.VLog
import com.google.gson.Gson
import org.json.JSONObject
import javax.inject.Inject


class SupportInteractImpl @Inject constructor(
	private val greenAppService: GreenAppService,
	private val prefs: PrefsInteract,
	private val gson: Gson
) :
	SupportInteract {

	@RequiresApi(Build.VERSION_CODES.N)
	override suspend fun getFAQQuestionAnswers(): Resource<List<FAQItem>> {
		try {
			val langCode = prefs.getSettingString(PrefsManager.CUR_LANGUAGE_CODE, "ru")
			val res = greenAppService.getFAQQuestionAnswer(langCode)
			if (res.isSuccessful) {
				val jsonResponse = JSONObject(res.body()!!.toString())
				val success = jsonResponse.getBoolean("success")
				if (success) {
					val jsonObject = JSONObject(
						res.body()!!.getAsJsonObject("result").getAsJsonObject("list").toString()
					)

					val faqItemList = mutableListOf<FAQItem>()
					jsonObject.keys().forEachRemaining {
						val jsonArray = jsonObject.getJSONArray(it)
						for (i in 0 until jsonArray.length()) {
							val questionAnswer = jsonArray.get(i) as JSONObject
							val question = questionAnswer.getString("question")
							val answer = questionAnswer.getString("answer")
							val faqItem = FAQItem(question, answer, expanded = false)
							faqItemList.add(faqItem)
						}
					}
					return Resource.success(faqItemList)
				} else {
					val error_code = jsonResponse.getInt("error_code")
					parseException(error_code)
				}
			} else {
				VLog.d("Request is not successful : ${res.message()}")
			}
		} catch (ex: Exception) {
			VLog.d("Exception throws in getting faq list : ${ex.message}")
			return Resource.error(ex)
		}
		return Resource.error(Exception("Exception thrown in faq list"))
	}

	override suspend fun postListing(listing: ListingPost): Resource<String> {
		try {

			val res = greenAppService.postListing(
				listing.name,
				listing.email,
				listing.project_name,
				listing.description,
				listing.blockChain,
				listing.twitter
			)
			if (res.isSuccessful) {

				VLog.d("Request is success for positing listing")

				val baseResponse = res.body()!!.success
				if (baseResponse) {

					VLog.d("BaseResponse is success for positing listing")

					return Resource.success("OK")
				} else {
					VLog.d("Base Response is no success")
				}

			} else {
				VLog.d("Response is no success in posting listing : $res")
			}
		} catch (ex: java.lang.Exception) {
			VLog.d("Exception occurred in posting listing : $ex")
			return Resource.error(ex)
		}
		return Resource.error(Exception("NO"))
	}

	override suspend fun postQuestion(question: QuestionPost): Resource<String> {
		try {
			val res = greenAppService.postQuestion(question.name, question.email, question.question)
			if (res.isSuccessful) {
				VLog.d("Request is success for positing question")
				val baseResponse = res.body()!!.success
				if (baseResponse) {
					VLog.d("BaseResponse is success for posting question")
					return Resource.success("OK")
				} else {
					parseException(res.body()!!.error_code!!)
				}
			} else {
				VLog.d("Question is not successful : $res")
			}
		} catch (ex: Exception) {
			VLog.d("Exception occurred in posting question : $ex")
			return Resource.error(ex)
		}
		return Resource.error(Exception("NO"))
	}


}
