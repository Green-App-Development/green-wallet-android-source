package com.green.wallet.presentation.main.transaction.btmsheets

import androidx.compose.runtime.Composable
import androidx.core.os.bundleOf
import com.green.wallet.domain.domainmodel.Transaction
import com.green.wallet.presentation.main.transaction.SpeedyTransactionBtmCatScreen
import com.greenwallet.core.base.BaseBottomSheetDialogFragment

class SpeedyBtmCatDialog : BaseBottomSheetDialogFragment() {

    @Composable
    override fun SetUI() {
        SpeedyTransactionBtmCatScreen()
    }

    companion object {

        const val TRANSACTION_KEY = "transaction_key"

        fun build(transaction: Transaction?) = SpeedyBtmCatDialog().apply {
            bundleOf(TRANSACTION_KEY to transaction)
        }
    }

}