package com.green.wallet.presentation.main.swap.request

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.green.wallet.databinding.FragmentRequestBinding
import dagger.android.support.DaggerFragment

class RequestFragment : DaggerFragment() {

	private lateinit var binding: FragmentRequestBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentRequestBinding.inflate(layoutInflater)
		binding.registerClicks()
		return binding.root
	}

	private fun FragmentRequestBinding.registerClicks() {

	}


}
