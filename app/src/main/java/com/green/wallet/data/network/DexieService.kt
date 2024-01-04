package com.green.wallet.data.network

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface DexieService {

    @GET("offers/{offer_id}")
    suspend fun getTibetSwapOfferStatus(@Path(value = "offer_id") offer_id: String): Response<JsonObject>

    @GET("status")
    suspend fun getDexieStatus(): Response<JsonObject>

}
