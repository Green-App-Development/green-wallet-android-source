package com.green.wallet.presentation.main.managewallet

sealed interface ManageWalletEvent {

    data class ShowData(val visible: Boolean) : ManageWalletEvent

}