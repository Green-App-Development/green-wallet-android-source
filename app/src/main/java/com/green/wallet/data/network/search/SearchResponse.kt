package com.green.wallet.data.network.search

class SearchResponse(
    val suggestions: List<SearchItem>
)

class SearchItem(
    val value: String
)