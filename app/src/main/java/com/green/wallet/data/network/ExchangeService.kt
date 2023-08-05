package com.green.wallet.data.network

import com.google.gson.JsonObject
import com.green.wallet.data.network.dto.exchange.ExchangeDTO
import com.green.wallet.data.network.dto.exchangestatus.ExchangeStatus
import retrofit2.Response
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ExchangeService {

    @FormUrlEncoded
    @POST("exchange/create")
    @JvmSuppressWildcards
    suspend fun createExchangeRequest(
        @FieldMap fields: Map<String, Any>
    ): Response<JsonObject>

    @GET("exchange/status")
    suspend fun getStatusOfOrderExchange(
        @Query("user") user: String,
        @Query("order") order: String
    ): Response<ExchangeStatus>

    @GET("exchange")
    suspend fun getExchangeRequestRate(@Query("user") user: String): Response<ExchangeDTO>

    @GET("exchange/calc/amount")
    suspend fun calOutputPrice(
        @Query("give_coin") giveCoin: String,
        @Query("get_coin") getCoin: String,
        @Query("give_amount") giveAmount: String,
        @Query("rate") rate: String
    ): Response<JsonObject>

}
