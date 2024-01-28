package com.green.wallet.presentation.main.send

sealed interface TransferEvent {

    object OnSuccessTransfer : TransferEvent

    object OnErrorTransfer : TransferEvent

    object OnPinConfirmed : TransferEvent

}