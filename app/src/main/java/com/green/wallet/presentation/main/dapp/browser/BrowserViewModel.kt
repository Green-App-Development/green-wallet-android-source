package com.green.wallet.presentation.main.dapp.browser

import androidx.lifecycle.viewModelScope
import com.green.wallet.domain.domainmodel.DAppLink
import com.green.wallet.domain.interact.FirebaseInteract
import com.green.wallet.domain.interact.SearchInteract
import com.green.wallet.presentation.di.appState.ConnectedDApp
import com.greenwallet.core.base.BaseViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class BrowserViewModel @Inject constructor(
    private val connectedDApp: ConnectedDApp,
    private val firebaseInteract: FirebaseInteract,
    private val searchInteract: SearchInteract
) : BaseViewModel<BrowserState, BrowserEvent>(
    BrowserState()
) {

    private var allDAppList = listOf<DAppLink>()

    init {
        viewModelScope.launch {
            allDAppList = firebaseInteract.getListOfDAppLink()
            handleEvent(
                BrowserEvent.OnChangeCategory(
                    category = SearchCategory.ALL
                )
            )
        }
    }

    fun handleEvent(event: BrowserEvent) {
        when (event) {
            is BrowserEvent.OnSearchChange -> {
                _viewState.update {
                    it.copy(
                        searchText = event.text
                    )
                }

                retrieveSuggestions(event.text)
            }

            is BrowserEvent.OnChangeCategory -> {
                val filtered = mutableListOf<DAppLink>()
                allDAppList.forEach { app ->
                    app.category.forEach { belongs ->
                        if (event.category.name.lowercase() == belongs.lowercase()) {
                            filtered.add(app)
                        }
                    }
                }
                _viewState.update { it.copy(searchCategory = event.category, dAppList = filtered) }
            }

            else -> {
                setEvent(event)
            }
        }
    }

    private fun retrieveSuggestions(text: String) {
        if (text.isEmpty()) {
            _viewState.update { it.copy(authCompleteResult = listOf()) }
        } else {
            viewModelScope.launch {
                val result = searchInteract.getSearchResultList(text)
                _viewState.update { it.copy(authCompleteResult = result) }
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