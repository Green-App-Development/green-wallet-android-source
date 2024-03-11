package com.green.wallet.presentation.custom.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.green.compose.theme.GreenWalletTheme
import com.green.wallet.databinding.DialogContainerBinding
import com.green.wallet.presentation.di.factory.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

abstract class BaseBottomSheetDialogFragment<V : ViewModel>(private val viewModelClass: Class<V>) :
    BottomSheetDialogFragment() {

    @Inject
    lateinit var factory: ViewModelFactory
    protected val vm: V by lazy {
        ViewModelProvider(this, factory)[viewModelClass]
    }

    lateinit var binding: DialogContainerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                collectFlowOnStarted(this)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogContainerBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.composeView.setContent {
            GreenWalletTheme {
                SetUI()
            }
        }
    }

    @Composable
    abstract fun SetUI()

    protected open fun collectFlowOnStarted(scope: CoroutineScope) = Unit


}