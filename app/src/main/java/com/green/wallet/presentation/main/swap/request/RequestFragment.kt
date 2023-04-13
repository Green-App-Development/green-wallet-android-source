package com.green.wallet.presentation.main.swap.request

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.green.wallet.databinding.FragmentRequestBinding
import com.green.wallet.domain.domainmodel.RequestItem
import com.green.wallet.presentation.tools.RequestStatus
import com.green.wallet.presentation.tools.getMainActivity
import dagger.android.support.DaggerFragment

class RequestFragment : DaggerFragment(), RequestItemAdapter.OnClickRequestItemListener {

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
		val requestAdapter = RequestItemAdapter(getMainActivity(), this)
		with(binding.recViewRequests) {
			adapter = requestAdapter
			layoutManager = LinearLayoutManager(getMainActivity())
		}
		val list = mutableListOf<RequestItem>()
		list.add(
			RequestItem("001766", RequestStatus.Cancelled, 16.00, 0.45, System.currentTimeMillis())
		)
		list.add(
			RequestItem("001765", RequestStatus.Waiting, 145.00, 4.45, System.currentTimeMillis())
		)
		list.add(
			RequestItem(
				"001710",
				RequestStatus.InProgress,
				145.00,
				4.45,
				System.currentTimeMillis()
			)
		)
		list.add(
			RequestItem(
				"001769",
				RequestStatus.Completed,
				145.00,
				4.45,
				System.currentTimeMillis()
			)
		)
		requestAdapter.updateRequestList(list)
	}

	private fun FragmentRequestBinding.registerClicks() {

	}

	override fun onClickDetailItem(item: RequestItem) {

	}


}
