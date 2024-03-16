package com.green.wallet.data.network

import com.green.wallet.data.network.search.SearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SearchService {

    @GET("suggest/{api_key}")
    suspend fun getSearchResult(
        @Path("api_key") apiKey: String,
        @Query("term") term: String,
    ): Response<SearchResponse>

}