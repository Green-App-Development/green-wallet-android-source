package com.green.wallet.presentation.main.swap.tibetswapdetail

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.common.tools.formatString
import com.example.common.tools.formattedTimeForOrderItem
import com.example.common.tools.getRequestStatusTranslation
import com.green.wallet.R
import com.green.wallet.databinding.FragmentTibetSwapDetailBinding
import com.green.wallet.databinding.FragmentTibetswapBinding
import com.green.wallet.domain.domainmodel.TibetSwapExchange
import com.green.wallet.presentation.custom.formattedDoubleAmountWithPrecision
import com.green.wallet.presentation.tools.VLog
import com.green.wallet.presentation.tools.getMainActivity
import com.green.wallet.presentation.tools.getStringResource
import dagger.android.support.DaggerFragment

class TibetSwapDetailFragment : DaggerFragment() {

	companion object {
		const val TIBET_SWAP_OFFER_KEY = "TIBET_SWAP_OFFER_KEY"
	}

	private lateinit var binding: FragmentTibetSwapDetailBinding
	private lateinit var tibetSwap: TibetSwapExchange

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		arguments?.let {
			val swap = arguments!!.getParcelable<TibetSwapExchange>(TIBET_SWAP_OFFER_KEY)
			if (swap != null)
				tibetSwap = swap
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentTibetSwapDetailBinding.inflate(layoutInflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		VLog.d("On Tibet Swap Detail ")
		if (this::tibetSwap.isInitialized)
			binding.initTibetSwapDetail(tibetSwap)
	}

	@SuppressLint("SetTextI18n")
	private fun FragmentTibetSwapDetailBinding.initTibetSwapDetail(item: TibetSwapExchange) {
		edtData.text = formattedTimeForOrderItem(item.time_created)
		val status = item.status
		txtStatus.text = "${getMainActivity().getStringResource(R.string.status_title)}: ${
			getRequestStatusTranslation(
				getMainActivity(),
				status
			)
		}"
		edtSendAmount.text =
			formattedDoubleAmountWithPrecision(item.send_amount) + " ${item.send_coin}"
		edtReceiveAmount.text =
			formattedDoubleAmountWithPrecision(item.receive_amount) + " ${item.receive_coin}"
		edtHashTransaction.text = formatString(7, item.offer_id, 4)
		edtCommissionNetwork.text = formattedDoubleAmountWithPrecision(item.fee)
		if (item.height != 0) {
			edtBlockHeight.text = item.height.toString()
		}
	}


}
