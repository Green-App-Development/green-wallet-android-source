package com.green.wallet.presentation.main.swap.tibetswap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.green.wallet.R
import com.green.wallet.databinding.DialogBtmCreateOfferBinding
import com.green.wallet.presentation.App
import com.green.wallet.presentation.custom.AnimationManager
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.tools.VLog
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
