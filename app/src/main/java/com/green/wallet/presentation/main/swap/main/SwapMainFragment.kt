package com.green.wallet.presentation.main.swap.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.green.wallet.R
import com.green.wallet.databinding.FragmentSwapMainBinding
import com.green.wallet.databinding.FragmentWalletSettingsBinding
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
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
//		navController = findNavController()
	}


}
