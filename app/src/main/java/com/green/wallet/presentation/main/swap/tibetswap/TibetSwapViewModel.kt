package com.green.wallet.presentation.main.swap.tibetswap

import androidx.lifecycle.ViewModel
import com.green.wallet.presentation.tools.VLog
import javax.inject.Inject

class TibetSwapViewModel
@Inject constructor(

) : ViewModel() {

	var isShowingSwap = true

	var xchToCAT = true
	var isShowingDetailsTran = false

	init {
		VLog.d("On create tibet vm : $this")
	}


	override fun onCleared() {
		super.onCleared()
		VLog.d("On cleared tibet vm : $this")
	}

}
