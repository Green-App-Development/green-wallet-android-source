package com.green.wallet.presentation.main.swap.exchange

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.green.wallet.databinding.FragmentExchangeBinding
import dagger.android.support.DaggerFragment

class ExchangeFragment : DaggerFragment() {

	private lateinit var binding: FragmentExchangeBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

	}


	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentExchangeBinding.inflate(layoutInflater)
		return binding.root
	}


	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

	}

}
