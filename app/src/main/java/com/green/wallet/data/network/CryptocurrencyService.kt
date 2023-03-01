package com.green.wallet.data.network

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url


interface CryptocurrencyService {

    @GET
    suspend fun getLatestCurrency(@Url url: String): Response<JsonObject>


}
