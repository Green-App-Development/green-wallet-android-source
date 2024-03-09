package com.green.wallet.presentation.main.dapp.browser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.green.wallet.presentation.custom.base.BaseComposeFragment
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.main.dapp.trade.TraderViewModel
import com.green.wallet.presentation.tools.VLog
import com.green.wallet.presentation.tools.getMainActivity
import com.greenwallet.core.ext.collectFlow
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

class BrowserFragment : BaseComposeFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: BrowserViewModel by viewModels { viewModelFactory }

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
                    getMainActivity().move2TraderFragment()
                }

                else -> Unit
            }
        }
    }

}