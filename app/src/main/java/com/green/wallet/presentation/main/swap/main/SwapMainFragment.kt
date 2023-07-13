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
		VLog.d("On CreateView SwapMainFragment")
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		VLog.d("On ViewCreated SwapMainFragment : $vm and PrevDest : ${vm.prevDestID}")
		navController =
			(childFragmentManager.findFragmentById(R.id.my_nav_swap) as NavHostFragment).navController
		vm.initSwapNavController(navController)
		binding.registerClicks()
	}


	private fun FragmentSwapMainBinding.registerClicks() {

		//0
		txtExchange.setOnClickListener {
			vm.navigateTo(vm.destList[0])
		}

		//1
		txtTibetSwap.setOnClickListener {
			vm.navigateTo(vm.destList[1])
		}

		//2
		txtMyRequests.setOnClickListener {
			vm.navigateTo(vm.destList[2])
		}

		vm.addDestId(R.id.fragment_exchange)
		if (vm.prevDestID != -1)
			vm.navigateTo(vm.prevDestID)

		navController.addOnDestinationChangedListener { controller, destination, arguments ->
			clickedChosenDestinations(
				destination.id
			)
		}

	}

	private fun FragmentSwapMainBinding.clickedChosenDestinations(
		dest: Int
	): Boolean {
		val txtClicks = listOf(txtExchange, txtTibetSwap, txtMyRequests)
		for (i in 0 until vm.destList.size) {
			if (vm.destList[i] == dest) {
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
