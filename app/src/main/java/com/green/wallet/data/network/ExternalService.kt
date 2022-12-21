package com.green.wallet.data.network

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path


interface ExternalService {

    @GET("{address}")
    suspend fun getCATBalance(@Path("address") address: String): Response<JsonObject>

}
