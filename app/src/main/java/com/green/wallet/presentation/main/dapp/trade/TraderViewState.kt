package com.green.wallet.presentation.main.dapp.trade

data class TraderViewState(
    val isLoading: Boolean = false,
    val isConnected: Boolean = false,
    val waitingConnection: Boolean = true
)