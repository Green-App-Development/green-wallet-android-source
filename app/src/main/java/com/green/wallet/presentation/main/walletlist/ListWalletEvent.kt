package com.green.wallet.presentation.main.walletlist

sealed interface ListWalletEvent {

    object PinCodeConfirmToDelete : ListWalletEvent

}