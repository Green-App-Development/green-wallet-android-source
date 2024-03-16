package com.green.wallet.domain.interact

interface SearchInteract {

    suspend fun getSearchResultList(prefix: String): List<String>

}