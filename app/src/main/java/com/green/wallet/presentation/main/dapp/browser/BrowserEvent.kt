package com.green.wallet.presentation.main.dapp.browser

sealed interface BrowserEvent {

    data class OnSearchChange(val text: String) : BrowserEvent

    data class OnChangeCategory(val category: SearchCategory) : BrowserEvent

    data class OnChooseDAppLink(val link: String) : BrowserEvent

    data class OnSearchIconClick(val searchText: String) : BrowserEvent

    object ListingApplication : BrowserEvent

}