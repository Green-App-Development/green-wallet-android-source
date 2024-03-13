package com.green.wallet.presentation.main.dapp.browser

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.green.wallet.presentation.custom.base.BaseComposeFragment
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.tools.getMainActivity
import com.greenwallet.core.ext.collectFlow
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

class BrowserFragment : BaseComposeFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: BrowserViewModel by viewModels { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.checkForConnectedDApps()
    }

    @Composable
    override fun SetUI() {
        val state by viewModel.viewState.collectAsStateWithLifecycle()
        BrowserScreen(
            state = state,
            onEvent = viewModel::handleEvent
        )
    }

    override fun collectFlowOnCreated(scope: CoroutineScope) {
        viewModel.event.collectFlow(scope) {
            when (it) {
                is BrowserEvent.ListingApplication -> {
                    getMainActivity().move2ListingFragment()
                }

                is BrowserEvent.ChosenDexie -> {
                    getMainActivity().move2TraderFragment("file:///android_asset/index.html")
                }

                is BrowserEvent.OnSearchIconClick -> {
                    getMainActivity().move2TraderFragment(viewModel.viewState.value.searchText)
                }
                
                else -> Unit
            }
        }
    }

}