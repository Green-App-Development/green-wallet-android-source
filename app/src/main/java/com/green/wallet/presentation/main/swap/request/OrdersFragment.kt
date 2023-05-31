package com.green.wallet.presentation.main.swap.request

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.green.wallet.databinding.FragmentRequestBinding
import com.green.wallet.domain.domainmodel.RequestItem
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.main.swap.main.SwapMainViewModel
import com.green.wallet.presentation.tools.OrderStatus
import com.green.wallet.presentation.tools.VLog
import com.green.wallet.presentation.tools.getMainActivity
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class OrdersFragment : DaggerFragment(), OrderItemAdapter.OnClickRequestItemListener {

	private lateinit var binding: FragmentRequestBinding

	@Inject
	lateinit var viewModelFactory: ViewModelFactory
	private val vm: OrdersViewModel by viewModels { viewModelFactory }
	private val swapMainSharedVM: SwapMainViewModel by viewModels { viewModelFactory }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		swapMainSharedVM.showingExchange = false
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
		val requestAdapter = OrderItemAdapter(getMainActivity(), this)
		with(binding.recViewRequests) {
			adapter = requestAdapter
			layoutManager = LinearLayoutManager(getMainActivity())
		}
		val list = mutableListOf<RequestItem>()
		list.add(
			RequestItem("001766", OrderStatus.Cancelled, 16.00, 0.45, System.currentTimeMillis())
		)
		list.add(
			RequestItem("001765", OrderStatus.Waiting, 145.00, 4.45, System.currentTimeMillis())
		)
		list.add(
			RequestItem(
				"001710",
				OrderStatus.InProgress,
				145.00,
				4.45,
				System.currentTimeMillis()
			)
		)
		list.add(
			RequestItem(
				"001769",
				OrderStatus.Completed,
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
		VLog.d("Request Item Detail : $item clicked")
		getMainActivity().move2OrderDetailsFragment(item.status)
	}


}
