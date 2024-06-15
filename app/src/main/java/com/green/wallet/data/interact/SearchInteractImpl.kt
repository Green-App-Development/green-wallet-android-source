package com.green.wallet.data.interact

import com.example.common.tools.parseQuery
import com.green.wallet.data.network.SearchService
import com.green.wallet.domain.interact.SearchInteract
import com.green.wallet.presentation.tools.VLog
import javax.inject.Inject

class SearchInteractImpl @Inject constructor(
    private val searchService: SearchService
) : SearchInteract {

    override suspend fun getSearchResultList(prefix: String): List<String> {
        try {
            val response = searchService.getSearchResult(prefix).string()
            return parseQuery(response)
        } catch (ex: Exception) {
            VLog.d("Exception while getting search result : ${ex.message}")
        }
        return emptyList()
    }

}