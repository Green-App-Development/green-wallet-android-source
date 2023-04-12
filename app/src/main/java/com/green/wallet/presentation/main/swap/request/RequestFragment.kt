package com.green.wallet.presentation.main.swap.request

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.green.wallet.databinding.FragmentRequestBinding
import com.green.wallet.presentation.tools.getMainActivity
import dagger.android.support.DaggerFragment

class RequestFragment : DaggerFragment() {

	private lateinit var binding: FragmentRequestBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

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


	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initRequestAdapter()
	}

	private fun initRequestAdapter() {
		val requestAdapter = RequestItemAdapter(getMainActivity())
		with(binding.recViewRequests) {
			adapter = requestAdapter
			layoutManager = LinearLayoutManager(getMainActivity())
		}
		requestAdapter.updateRequestList(listOf())
	}

	private fun FragmentRequestBinding.registerClicks() {

	}


}
