package com.green.wallet.presentation.main.swap.main

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.green.wallet.R
import com.green.wallet.presentation.tools.VLog
import javax.inject.Inject

class SwapMainViewModel @Inject constructor() : ViewModel() {

	lateinit var swapNavController: NavController
	var showingExchange = false
	var prevDestID: Int = -1

	val visitedDest = mutableSetOf<Int>()
	fun initSwapNavController(navController: NavController) {
		this.swapNavController = navController
	}

	init {
		VLog.d("Swap Main View Model : $this is created")
	}

	val destList =
		listOf(R.id.fragment_exchange, R.id.fragment_tibet_swap, R.id.fragment_request)

	fun move2RequestHistory() {
		visitedDest.add(R.id.fragment_request)
		prevDestID = R.id.fragment_request
		swapNavController.navigate(R.id.fragment_request)
	}

	fun navigateTo(destId: Int) {
		prevDestID = destId
		if (visitedDest.contains(destId)) {
			while (swapNavController.currentDestination?.id != destId) {
				visitedDest.remove(swapNavController.currentDestination?.id)
				swapNavController.popBackStack()
			}
		} else {
			visitedDest.add(destId)
			swapNavController.navigate(destId)
		}
	}

	fun addDestId(destId: Int) {
		visitedDest.add(destId)
	}

	override fun onCleared() {
		super.onCleared()
		visitedDest.clear()
		VLog.d("Swap Main View Model : $this is cleared")
	}

}
