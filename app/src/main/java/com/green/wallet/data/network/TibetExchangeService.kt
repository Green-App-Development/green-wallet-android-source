package com.green.wallet.data.network

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.GET

interface TibetExchangeService {

	@GET("tokens")
	suspend fun getTokensWithPairID(): Response<JsonArray>

}
