package com.green.wallet.presentation.main.swap.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.green.wallet.R
import com.green.wallet.databinding.FragmentSwapMainBinding
import com.green.wallet.presentation.App
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.tools.VLog
import com.green.wallet.presentation.tools.getColorResource
import com.green.wallet.presentation.tools.getMainActivity
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_swap_main.*
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
			if (destination.id == R.id.fragment_exchange) {
				txtClicked(txtExchange)
				txtUnClicked(txtMyRequests)
			} else {
				txtClicked(txtMyRequests)
				txtUnClicked(txtExchange)
			}
		}
	}

	private fun FragmentSwapMainBinding.registerClicks() {
		txtExchange.setOnClickListener {
			if (vm.showingExchange) return@setOnClickListener
			vm.showingExchange = true
			navController.popBackStack()
		}
		txtMyRequests.setOnClickListener {
			if (!vm.showingExchange) return@setOnClickListener
			vm.showingExchange = false
			navController.navigate(R.id.fragment_request)
		}
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
