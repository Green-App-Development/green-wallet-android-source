package com.green.wallet.presentation.main.swap.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.green.wallet.R
import com.green.wallet.databinding.FragmentSwapMainBinding
import com.green.wallet.presentation.tools.getColorResource
import com.green.wallet.presentation.tools.getMainActivity
import dagger.android.support.DaggerFragment

class SwapMainFragment : DaggerFragment() {

	private lateinit var binding: FragmentSwapMainBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
	}

	private lateinit var navController: NavController

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentSwapMainBinding.inflate(layoutInflater)
		binding.registerClicks()
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		navController =
			(childFragmentManager.findFragmentById(R.id.my_nav_swap) as NavHostFragment).navController
	}

	private var showingExchange = true

	private fun FragmentSwapMainBinding.registerClicks() {
		txtClicked(txtExchange)
		txtExchange.setOnClickListener {
			if (showingExchange) return@setOnClickListener
			showingExchange = true
			txtClicked(txtExchange)
			txtUnClicked(txtMyRequests)
			navController.navigate(R.id.fragment_exchange)
		}
		txtMyRequests.setOnClickListener {
			if (!showingExchange) return@setOnClickListener
			showingExchange = false
			txtUnClicked(txtExchange)
			txtClicked(txtMyRequests)
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


}
