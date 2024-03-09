package com.green.wallet.presentation.main.dapp.browser

sealed interface BrowserEvent {

    data class OnSearchChange(val text: String) : BrowserEvent

    data class OnChangeCategory(val category: SearchCategory) : BrowserEvent

    object ListingApplication : BrowserEvent

    object ChosenDexie : BrowserEvent

}