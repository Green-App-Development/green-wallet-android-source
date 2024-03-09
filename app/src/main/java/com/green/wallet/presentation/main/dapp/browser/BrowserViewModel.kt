package com.green.wallet.presentation.main.dapp.browser

import com.green.wallet.R
import com.green.wallet.domain.domainmodel.DAppModel
import com.greenwallet.core.base.BaseViewModel
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class BrowserViewModel @Inject constructor() :
    BaseViewModel<BrowserState, BrowserEvent>(
        BrowserState()
    ) {

    private val dAppList = listOf(
        DAppModel(
            name = "Dexie",
            description = "Описание которе будет меняться под каждый из сервисов",
            url = "dexies.space",
            resource = R.drawable.ic_chia
        )
    )

    fun handleEvent(event: BrowserEvent) {
        when (event) {
            is BrowserEvent.OnSearchChange -> {
                val filtered = dAppList.filter { it.name.contains(event.text, ignoreCase = true) }
                _viewState.update { it.copy(searchText = event.text, dAppList = filtered) }
            }

            is BrowserEvent.OnChangeCategory -> {
                if (_viewState.value.searchCategory != event.category) {
                    _viewState.update { it.copy(searchCategory = event.category) }
                }
            }

            else -> {
                setEvent(event)
            }
        }
    }


}