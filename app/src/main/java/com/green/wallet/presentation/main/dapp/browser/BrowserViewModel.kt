package com.green.wallet.presentation.main.dapp.browser

import com.green.wallet.R
import com.green.wallet.domain.domainmodel.DAppModel
import com.green.wallet.presentation.di.appState.ConnectedDApp
import com.greenwallet.core.base.BaseViewModel
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class BrowserViewModel @Inject constructor(
    private val connectedDApp: ConnectedDApp
) : BaseViewModel<BrowserState, BrowserEvent>(
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
                _viewState.update { it.copy(searchText = event.text) }
            }

            is BrowserEvent.OnChangeCategory -> {
//                if (_viewState.value.searchCategory != event.category) {
//                    _viewState.update { it.copy(searchCategory = event.category) }
//                }
            }

            else -> {
                setEvent(event)
            }
        }
    }

    fun checkForConnectedDApps() {
        _viewState.update {
            it.copy(
                dAppList = it.dAppList.map {
                    it.copy(
                        isConnected = connectedDApp.connected.contains(
                            it.name
                        )
                    )
                }
            )
        }
    }


}