package com.green.wallet.presentation.main.swap.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.green.wallet.R
import com.green.wallet.databinding.FragmentSwapMainBinding
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.tools.VLog
import com.green.wallet.presentation.tools.getColorResource
import com.green.wallet.presentation.tools.getMainActivity
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class SwapMainFragment : DaggerFragment() {

	private lateinit var binding: FragmentSwapMainBinding

	@Inject
	lateinit var viewModelFactory: ViewModelFactory
	private val vm: SwapMainViewModel by viewModels { viewModelFactory }


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		VLog.d("On Create SwapMainFragment")
	}

	private lateinit var navController: NavController

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentSwapMainBinding.inflate(layoutInflater)
		binding.registerClicks()
		VLog.d("On CreateView SwapMainFragment")
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		VLog.d("On ViewCreated SwapMainFragment")
		navController =
			(childFragmentManager.findFragmentById(R.id.my_nav_swap) as NavHostFragment).navController
		vm.initSwapNavController(navController)
		registerNavListener()
	}

	private fun registerNavListener() {
		navController.addOnDestinationChangedListener { controller, destination, arguments ->
			binding.clickedChosenDestinations(destination.id)
		}
		if (vm.prevDest != -1) {
			navController.navigate(vm.prevDest)
		}
	}

	private fun FragmentSwapMainBinding.registerClicks() {

		//0
		txtExchange.setOnClickListener {
			clickedChosenDestinations(R.id.fragment_exchange)
			navController.popBackStack(R.id.fragment_exchange, false)
			vm.prevVisitedDest.clear()
		}

		//1
		txtMyRequests.setOnClickListener {
			if (clickedChosenDestinations(R.id.fragment_request)) {
				if (!vm.prevVisitedDest.contains(1)) {
					vm.prevVisitedDest.add(1)
					navController.navigate(R.id.fragment_request)
				} else {
					val res = navController.popBackStack(
						R.id.fragment_request,
						false
					)
					if (res)
						vm.prevVisitedDest.remove(2)
					VLog.d(
						"Already have been to fragment request  : $res"
					)
				}
			}
		}

		//2
		txtTibetSwap.setOnClickListener {
			if (clickedChosenDestinations(R.id.fragment_tibet_swap)) {
				if (!vm.prevVisitedDest.contains(2)) {
					vm.prevVisitedDest.add(2)
					navController.navigate(R.id.fragment_tibet_swap)
				} else {
					val res = navController.popBackStack(
						R.id.fragment_tibet_swap,
						false
					)
					if (res)
						vm.prevVisitedDest.remove(1)
					VLog.d(
						"Already have been to tibet fragment swap : $res"
					)
				}
			}
		}


	}

	private fun FragmentSwapMainBinding.clickedChosenDestinations(dest: Int): Boolean {
		if (vm.prevDest == dest)
			return false
		vm.prevDest = dest
		val destList =
			listOf(R.id.fragment_exchange, R.id.fragment_tibet_swap, R.id.fragment_request)
		val txtClicks = listOf(txtExchange, txtTibetSwap, txtMyRequests)
		for (i in 0 until destList.size) {
			if (destList[i] == dest) {
				txtClicked(txtClicks[i])
			} else {
				txtUnClicked(txtClicks[i])
			}
		}
		return true
	}

	private fun txtClicked(txt: TextView?) {
		txt?.apply {
			background.setTint(getMainActivity().getColorResource(R.color.green))
			setTextColor(getMainActivity().getColorResource(R.color.white))
		}
	}

	private fun txtUnClicked(txt: TextView?) {
		txt?.apply {
			background.setTint(getMainActivity().getColorResource(R.color.bcg_sorting_txt_category))
			setTextColor(getMainActivity().getColorResource(R.color.sorting_txt_category))
		}
	}

	override fun onStart() {
		super.onStart()
		VLog.d("On Start SwapMainFragment")
	}


	override fun onResume() {
		super.onResume()
		VLog.d("On Resume SwapMainFragment")
	}

	override fun onStop() {
		super.onStop()
		VLog.d("On Stop SwapMainFragment")
	}

	override fun onPause() {
		super.onPause()
		VLog.d("On Pause SwapMainFragment")
	}


	override fun onDestroy() {
		super.onDestroy()
		VLog.d("On Destroy SwapMainFragment")
	}

}
