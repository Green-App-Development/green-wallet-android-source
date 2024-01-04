package com.green.wallet.presentation.custom.base

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.android.support.DaggerFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

abstract class BaseFragment : DaggerFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                collectFlowOnStarted(this)
            }
        }
    }

    open fun collectFlowOnStarted(scope: CoroutineScope) {}


}