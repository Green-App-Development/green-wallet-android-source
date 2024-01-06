package com.green.wallet.presentation.main.transaction.btmSpeedy

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.os.bundleOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.green.wallet.R
import com.green.wallet.domain.domainmodel.Transaction
import com.green.wallet.presentation.custom.base.BaseBottomSheetDialogFragment

class SpeedyBtmDialog :
    BaseBottomSheetDialogFragment<SpeedyDialogViewModel>(SpeedyDialogViewModel::class.java) {

    private val transaction: Transaction? by lazy {
        arguments?.getParcelable(TRANSACTION_KEY)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme)
    }

    @Composable
    override fun SetUI() {
        vm.setCurTransaction(transaction)
        val state by vm.viewState.collectAsStateWithLifecycle()
        SpeedyTransactionBtmScreen(
            state = state
        )
    }

    companion object {

        private const val TRANSACTION_KEY = "transaction_key"

        fun build(transaction: Transaction?) = SpeedyBtmDialog().apply {
            val bundle = Bundle()
            bundle.putParcelable(TRANSACTION_KEY, transaction)
            arguments = bundle
        }
    }

}