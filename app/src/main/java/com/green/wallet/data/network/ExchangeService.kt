package com.green.wallet.data.network

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ExchangeService {

	@POST("exchange/create")
	suspend fun createExchangeRequest(
		@Query("user") user: String,
		@Query("give_address") give_address: String,
		@Query("give_amount") give_amount: Double,
		@Query("get_address") get_address: String
	): Response<JsonObject>

	@GET("/status")
	suspend fun getStatusOfExchangeRequest(
		@Query("user") user: String,
		@Query("order") order: String
	): Response<JsonObject>


}
