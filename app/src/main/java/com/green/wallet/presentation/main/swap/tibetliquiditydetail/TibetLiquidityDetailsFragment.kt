package com.green.wallet.presentation.main.swap.tibetliquiditydetail

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.common.tools.formatString
import com.example.common.tools.formattedTimeForOrderItem
import com.example.common.tools.getRequestStatusColor
import com.example.common.tools.getRequestStatusTranslation
import com.green.wallet.R
import com.green.wallet.databinding.FragmentTibetLiquidityDetailBinding
import com.green.wallet.databinding.FragmentTibetSwapDetailBinding
import com.green.wallet.domain.domainmodel.TibetLiquidity
import com.green.wallet.domain.domainmodel.TibetSwapExchange
import com.green.wallet.presentation.custom.formattedDollarWithPrecision
import com.green.wallet.presentation.custom.formattedDoubleAmountWithPrecision
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.tools.copyToClipBoard
import com.green.wallet.presentation.tools.getColorResource
import com.green.wallet.presentation.tools.getMainActivity
import com.green.wallet.presentation.tools.getStringResource
import dagger.android.support.DaggerFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class TibetLiquidityDetailsFragment : DaggerFragment() {

	@Inject
	lateinit var viewModelFactory: ViewModelFactory
	private val vm: TibetLiquidityDetailViewModel by viewModels { viewModelFactory }

	private lateinit var binding: FragmentTibetLiquidityDetailBinding

	companion object {
		const val TIBET_LIQUIDITY_OFFER_KEY = "TIBET_LIQUIDITY_OFFER_KEY"
	}

	var offerId = ""

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		arguments?.let {
			offerId = it.getString(TIBET_LIQUIDITY_OFFER_KEY, "")
		}
	}


	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentTibetLiquidityDetailBinding.inflate(layoutInflater)
		return binding.root
	}

	private fun initDetailTibetLiquidity() {
		lifecycleScope.launch {
			repeatOnLifecycle(Lifecycle.State.STARTED) {
				vm.getTibetLiquidityByOfferId(offerId).collectLatest {
					binding.initTibetLiquidityDetail(it)
				}
			}
		}
	}

	@SuppressLint("SetTextI18n")
	private fun FragmentTibetLiquidityDetailBinding.initTibetLiquidityDetail(item: TibetLiquidity) {
		edtData.text = formattedTimeForOrderItem(item.time_created)
		val status = item.status
		txtStatus.text = "${getMainActivity().getStringResource(R.string.status_title)}: ${
			getRequestStatusTranslation(
				getMainActivity(),
				status
			)
		}"
		val xchCat = "${formattedDoubleAmountWithPrecision(item.xchAmount)} XCH\n${
			formattedDoubleAmountWithPrecision(item.catAmount)
		} ${item.catToken}"
		val liquid =
			"${formattedDoubleAmountWithPrecision(item.liquidityAmount)} ${item.liquidityToken}"
		if (item.addLiquidity) {
			edtSendAmount.text = xchCat
			edtReceiveAmount.text = liquid
		} else {
			edtSendAmount.text = liquid
			edtReceiveAmount.text = xchCat
		}

		edtHashTransaction.text = formatString(7, item.offer_id, 4)
		edtCommissionNetwork.text = formattedDoubleAmountWithPrecision(item.fee)
		if (item.height != 0) {
			edtBlockHeight.text = item.height.toString()
		}
		imgCpyHashTransaction.setOnClickListener {
			requireActivity().copyToClipBoard(item.offer_id)
		}
		changeColorTxtStatusRequest(txtStatus, getRequestStatusColor(status, getMainActivity()))

		backLayout.setOnClickListener {
			getMainActivity().popBackStackOnce()
		}

	}

	private fun changeColorTxtStatusRequest(txt: TextView, clr: Int) {
		val text = txt.text.toString()
		val pivot = text.indexOf(":")
		val spannableString = SpannableString(text)
		val textPart =
			ForegroundColorSpan(getMainActivity().getColorResource(R.color.txt_status_request))
		val amountPart = ForegroundColorSpan(clr)
		spannableString.setSpan(textPart, 0, pivot, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
		spannableString.setSpan(
			amountPart,
			pivot + 1,
			text.length,
			Spanned.SPAN_INCLUSIVE_INCLUSIVE
		)
		txt.text = spannableString
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initDetailTibetLiquidity()
	}

}
