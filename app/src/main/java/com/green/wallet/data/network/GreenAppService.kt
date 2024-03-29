package com.green.wallet.data.network


import com.green.wallet.data.network.dto.greenapp.network.NetworkItemBaseResponse
import com.green.wallet.data.network.dto.greenapp.token.TokenBaseResponse
import com.green.wallet.data.network.dto.greenapp.lang.LanguageResponse
import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url


interface GreenAppService {


	@GET("localization/languages")
	suspend fun getLanguageList(): Response<LanguageResponse>


	@GET("localization/translate")
	suspend fun getLanguageTranslate(
		@Query("code") code: String,
		@Query("version") version: String
	): ResponseBody

	@GET("blockchains")
	suspend fun getAvailableBlockChains(): Response<NetworkItemBaseResponse>


	@GET("faq")
	suspend fun getFAQQuestionAnswer(@Query("code") code: String): Response<JsonObject>

	@POST("listing")
	suspend fun postListing(
		@Query("name") name: String,
		@Query("email") email: String,
		@Query("project_name") project_name: String,
		@Query("description") description: String,
		@Query("blockchain") blockchain: String,
		@Query("twitter") twitter: String,
	): Response<BaseResponse>


	@POST("support")
	suspend fun postQuestion(
		@Query("name") name: String,
		@Query("email") email: String,
		@Query("question") question: String
	): Response<BaseResponse>


	@GET("notifications")
	suspend fun getNotifOtherItems(@Query("code") code: String): Response<JsonObject>


	@GET("agreements")
	suspend fun getAgreementText(@Query("code") code: String): Response<JsonObject>


	@GET("tails")
	suspend fun getAllTails(@Query("blockchain") blockChain: String): Response<TokenBaseResponse>

	@GET("tails/price")
	suspend fun getAllTailsPrice(): Response<JsonObject>

	@GET("coins")
	suspend fun getCoinDetails(@Query("code") code: String): Response<JsonObject>


	@GET("server/time")
	suspend fun getServerTime(): Response<JsonObject>

	@GET
	suspend fun getUpdatedChiaChivesCourse(@Url url: String): Response<JsonObject>

	@GET("nft/verification")
	suspend fun getNFTVerificationList(@Query("blockchain") blockChain: String):Response<JsonObject>

}

























