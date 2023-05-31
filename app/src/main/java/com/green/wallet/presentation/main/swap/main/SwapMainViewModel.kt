package com.green.wallet.presentation.main.swap.main

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.green.wallet.R
import com.green.wallet.presentation.tools.VLog
import javax.inject.Inject

class SwapMainViewModel @Inject constructor() : ViewModel() {

	lateinit var swapNavController: NavController
	var showingExchange = false

	fun initSwapNavController(navController: NavController) {
		this.swapNavController = navController
	}

	init {
		VLog.d("Swap Main View Model : $this is created")
	}

	fun move2RequestHistory() {
		swapNavController.navigate(R.id.fragment_request)
	}

	override fun onCleared() {
		super.onCleared()
		VLog.d("Swap Main View Model : $this is cleared")
	}

}
