package com.green.wallet.presentation.main.swap.tibetswap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.common.tools.formatString
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.green.wallet.R
import com.green.wallet.databinding.DialogBtmCreateOfferBinding
import com.green.wallet.presentation.App
import com.green.wallet.presentation.custom.AnimationManager
import com.green.wallet.presentation.custom.formattedDollarWithPrecision
import com.green.wallet.presentation.custom.formattedDoubleAmountWithPrecision
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.tools.PRECISION_CAT
import com.green.wallet.presentation.tools.PRECISION_XCH
import com.green.wallet.presentation.tools.VLog
import com.green.wallet.presentation.tools.getColorResource
import com.green.wallet.presentation.tools.getMainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class BtmCreateOfferDialog : BottomSheetDialogFragment() {

	private lateinit var binding: DialogBtmCreateOfferBinding

	@Inject
	lateinit var viewModelFactory: ViewModelFactory
	private val vm: TibetSwapViewModel by viewModels { viewModelFactory }

	@Inject
	lateinit var animManager: AnimationManager

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		(requireActivity().application as App).appComponent.inject(this)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = DialogBtmCreateOfferBinding.inflate(layoutInflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		VLog.d("On View Created Create Offer with vm : $vm")
//		binding.clickedFeeOptions()
		binding.listeners()
	}


	private fun DialogBtmCreateOfferBinding.listeners() {

		relChosenLongClick.setOnClickListener {
			clickedPositionsFee(0)
		}

		relChosenMediumClick.setOnClickListener {
			clickedPositionsFee(1)
		}

		relChosenShortClick.setOnClickListener {
			clickedPositionsFee(2)
		}

		val tokenCode = vm.tokenList.value[vm.catAdapPosition].code
		txtCAT.text = tokenCode
		txtWalletAddress.text = formatString(10, vm.curWallet?.address ?: "", 6)
		if (vm.tibetSwap.value?.data != null) {
			val amountIn = formattedDoubleAmountWithPrecision((vm.tibetSwap.value!!.data?.amount_in
				?: 0L) / if (vm.xchToCAT) PRECISION_XCH else PRECISION_CAT)
			val amountOut = formattedDoubleAmountWithPrecision((vm.tibetSwap.value!!.data?.amount_out
				?: 0L) / if (vm.xchToCAT) PRECISION_CAT else PRECISION_XCH)
			if (vm.xchToCAT) {
				txtMinus(txtXCHValue, amountIn, "XCH")
				txtPlus(txtCATValue, amountOut, tokenCode)
			} else {
				txtPlus(txtXCHValue, amountIn, "XCH")
				txtMinus(txtCATValue, amountOut, tokenCode)
			}
		}

	}

	private fun txtPlus(txt: TextView, value: String, token: String) {
		txt.setText("+ $value $token")
		txt.setTextColor(requireActivity().getColorResource(R.color.green))
	}

	private fun txtMinus(txt: TextView, value: String, token: String) {
		txt.setText("- $value $token")
		txt.setTextColor(requireActivity().getColorResource(R.color.red_mnemonic))
	}

	private fun DialogBtmCreateOfferBinding.clickedPositionsFee(pos: Int) {
		val layouts = listOf(relChosenLong, relChosenMedium, relChosenShort)
		for (i in 0 until layouts.size) {
			if (i == pos) {
				layouts[i].visibility = View.VISIBLE
			} else {
				layouts[i].visibility = View.INVISIBLE
			}
		}
	}

	private fun changePositionsFeeCombinations(from: Int, to: Int, width: Int) {

		val key = "$from|$to"
		when (key) {
			"1|2" -> {

			}

			"1|3" -> {

			}

			"2|1" -> {

			}

			"2|3" -> {
//				animManager.moveViewToRightByWidth(binding.relChosenFee, width)
			}

			"3|1" -> {

			}

			"3|2" -> {

			}
		}

	}

	override fun getTheme(): Int {
		return R.style.AppBottomSheetDialogTheme
	}


}
