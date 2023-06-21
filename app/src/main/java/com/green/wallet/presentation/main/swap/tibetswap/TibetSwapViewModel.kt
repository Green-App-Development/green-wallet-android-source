package com.green.wallet.presentation.main.swap.tibetswap

import androidx.lifecycle.ViewModel
import javax.inject.Inject

class TibetSwapViewModel
@Inject constructor(

) : ViewModel() {

    var isShowingSwap = true

    var xchToCAT = true
    var isShowingDetailsTran = false
    var curChosenFee = 0


}
