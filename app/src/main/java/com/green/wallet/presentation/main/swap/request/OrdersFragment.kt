package com.green.wallet.presentation.main.swap.request

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.green.wallet.databinding.FragmentRequestBinding
import com.green.wallet.domain.domainmodel.RequestItem
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.main.swap.main.SwapMainViewModel
import com.green.wallet.presentation.tools.VLog
import com.green.wallet.presentation.tools.getMainActivity
import dagger.android.support.DaggerFragment
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
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
		initOrdersAdapter()
	}

	private var prevOrderListJob: Job? = null

	private fun initOrdersAdapter() {
		val orderAdapter = OrderItemAdapter(getMainActivity(), this)
		with(binding.recViewRequests) {
			adapter = orderAdapter
			layoutManager = LinearLayoutManager(getMainActivity())
		}
		prevOrderListJob = lifecycleScope.launchWhenCreated {
			repeatOnLifecycle(Lifecycle.State.STARTED) {
				vm.orderList.collectLatest {
					orderAdapter.updateRequestList(it)
				}
			}
		}
	}

	private fun FragmentRequestBinding.registerClicks() {

	}

	override fun onClickDetailItem(hash: String) {
		VLog.d("Request Item Detail : $hash clicked")
		getMainActivity().move2OrderDetailsFragment(hash)
	}


	override fun onDestroyView() {
		super.onDestroyView()
		prevOrderListJob?.cancel()
	}


}
