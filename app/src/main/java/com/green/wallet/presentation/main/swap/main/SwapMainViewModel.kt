package com.green.wallet.presentation.main.swap.main

import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.green.wallet.R
import com.green.wallet.presentation.main.swap.requestdetail.OrderDetailFragment
import com.green.wallet.presentation.main.swap.requestdetail.OrderDetailFragment.Companion.ORDER_HASH
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

	fun move2OrderDetailFragment(orderHash: String) {
		val bundle = bundleOf(ORDER_HASH to orderHash)
	}

	override fun onCleared() {
		super.onCleared()
		VLog.d("Swap Main View Model : $this is cleared")
	}

}
