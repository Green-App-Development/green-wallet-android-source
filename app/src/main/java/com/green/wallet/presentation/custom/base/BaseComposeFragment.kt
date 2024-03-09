package com.green.wallet.presentation.custom.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.green.compose.theme.GreenWalletTheme
import com.green.wallet.presentation.di.factory.ViewModelFactory
import dagger.android.support.DaggerFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

abstract class BaseComposeFragment : DaggerFragment() {

    private lateinit var composeView: ComposeView

    @Inject
    lateinit var factory: ViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                collectFlowOnCreated(this)
            }
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                collectFlowOnStart(this)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).also {
            composeView = it
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        composeView.setContent {
            GreenWalletTheme {
                SetUI()
            }
        }
    }

    @Composable
    protected abstract fun SetUI()

    protected open fun collectFlowOnStart(scope: CoroutineScope) = Unit
    protected open fun collectFlowOnCreated(scope: CoroutineScope) = Unit


}