package com.green.wallet.presentation.main.transaction

import com.green.wallet.domain.domainmodel.Transaction

sealed interface TransactionEvent {

    data class BottomSheetCAT(val transaction: Transaction?) : TransactionEvent

    object BottomSheetNFT : TransactionEvent


}