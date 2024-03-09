package com.green.wallet.presentation.main.dapp.browser

import com.green.wallet.R
import com.green.wallet.domain.domainmodel.DAppModel

data class BrowserState(
    val isLoading: Boolean = false,
    val searchText: String = "",
    val searchCategory: SearchCategory = SearchCategory.ALL,
    val dAppList: List<DAppModel> = listOf(
        DAppModel(
            name = "Dexie",
            description = "Описание которе будет меняться под каждый из сервисов",
            url = "dexies.space",
            resource = R.drawable.ic_chia
        )
    )
)