package com.green.wallet.presentation.main.transaction.btmCancel

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.green.wallet.presentation.App
import com.green.wallet.presentation.custom.base.BaseBottomSheetDialogFragment
import com.green.wallet.presentation.tools.METHOD_CHANNEL_GENERATE_HASH
import com.green.wallet.presentation.tools.VLog
import com.greenwallet.core.ext.collectFlow
import io.flutter.plugin.common.MethodChannel

class CancelOfferDialog :
    BaseBottomSheetDialogFragment<CancelOfferViewModel>(CancelOfferViewModel::class.java) {

    private val tranID: String by lazy {
        arguments?.getString(TRANSACTION_KEY) ?: ""
    }

    val methodChannel by lazy {
        MethodChannel(
            (context?.applicationContext as App).flutterEngine.dartExecutor.binaryMessenger,
            METHOD_CHANNEL_GENERATE_HASH
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm.initOfferTransaction(tranID)
    }

    @Composable
    override fun SetUI() {
        val state by vm.viewState.collectAsStateWithLifecycle()

        CancelOfferScreen(
            state = state,
            onEvent = vm::handleEvent
        )

        LaunchedEffect(true) {
            vm.event.collectFlow(this) {
                when (it) {
                    is CancelOfferEvent.OnSign -> {
                        
                        //need to call pin code
                    }

                    else -> Unit
                }
            }
        }
    }


    companion object {

        private const val TRANSACTION_KEY = "transaction_key"

        fun build(tranID: String) = CancelOfferDialog().apply {
            val bundle = Bundle()
            bundle.putString(TRANSACTION_KEY, tranID)
            arguments = bundle
        }
    }


}