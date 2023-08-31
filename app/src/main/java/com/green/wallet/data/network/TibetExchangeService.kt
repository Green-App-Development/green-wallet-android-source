package com.green.wallet.data.network

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.green.wallet.data.network.dto.tibet.TibetLiquidityDto
import com.green.wallet.data.network.dto.tibet.TibetSwapDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TibetExchangeService {

	@GET("tokens")
	suspend fun getTokensWithPairID(): ResponseBody

	@GET("quote/{pair_id}")
	suspend fun calculateAmountInOut(
		@Path("pair_id") pair_id: String,
		@Query("amount_in") amount_in: Long,
		@Query("xch_is_input") xch_is_input: Boolean
	): Response<TibetSwapDto>

	@POST("offer/{pair_id}")
	suspend fun pushingOfferToTibet(
		@Path("pair_id") pair_id: String,
		@Body body: HashMap<String, Any>
	): Response<JsonObject>

	@GET("pair/{pair_id}")
	suspend fun getTibetLiquidityByPairId(
		@Path("pair_id") pair_id: String
	): Response<TibetLiquidityDto>


}
