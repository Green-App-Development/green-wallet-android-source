package com.green.wallet.data.network

import com.google.gson.JsonObject
import com.green.wallet.data.network.search.SearchResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface SearchService {

    @GET("complete/search?client=chrome")
    suspend fun getSearchResult(
        @Query("q") word: String
    ): ResponseBody

    @GET
    suspend fun getAddressDaoName(
        @Url url: String
    ): JsonObject

}