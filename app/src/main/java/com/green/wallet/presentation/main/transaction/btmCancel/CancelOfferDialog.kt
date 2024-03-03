package com.green.wallet.presentation.main.transaction.btmCancel

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.green.wallet.presentation.custom.base.BaseBottomSheetDialogFragment
import com.green.wallet.presentation.tools.VLog
import com.greenwallet.core.ext.collectFlow

class CancelOfferDialog :
    BaseBottomSheetDialogFragment<CancelOfferViewModel>(CancelOfferViewModel::class.java) {

    private val tranID: String by lazy {
        arguments?.getString(TRANSACTION_KEY) ?: ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm.initOfferTransaction(tranID)
    }

    @Composable
    override fun SetUI() {
        val state by vm.viewState.collectAsStateWithLifecycle()

        VLog.d("Updating the state on cancel $state")

        CancelOfferScreen(
            state = state,
            onEvent = vm::handleEvent
        )

        LaunchedEffect(true) {
            vm.event.collectFlow(this) {
                when (it) {
                    is CancelOfferEvent.OnSign -> {

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