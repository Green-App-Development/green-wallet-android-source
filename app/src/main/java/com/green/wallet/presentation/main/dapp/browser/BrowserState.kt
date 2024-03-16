package com.green.wallet.presentation.main.dapp.browser

import com.green.wallet.R
import com.green.wallet.domain.domainmodel.DAppLink

data class BrowserState(
    val isLoading: Boolean = false,
    val searchText: String = "",
    val searchCategory: SearchCategory = SearchCategory.ALL,
    val dAppList: List<DAppLink> = listOf(),
    val authCompleteResult: List<String> = listOf()
)