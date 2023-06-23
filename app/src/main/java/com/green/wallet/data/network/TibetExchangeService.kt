package com.green.wallet.data.network

import com.google.gson.JsonArray
import com.green.wallet.data.network.dto.tibet.TibetSwapDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TibetExchangeService {

	@GET("tokens")
	suspend fun getTokensWithPairID(): Response<JsonArray>

	@GET("quote/{pair_id}")
	suspend fun calculateAmountInOut(
		@Path("pair_id") pair_id: String,
		@Query("amount_in") amount_in: Long,
		@Query("xch_is_input") xch_is_input: Boolean
	): Response<TibetSwapDto>


}
