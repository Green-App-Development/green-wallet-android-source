package com.green.wallet.presentation.main.swap.exchange

import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.AdapterView
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.green.wallet.databinding.FragmentExchangeBinding
import com.green.wallet.presentation.custom.AnimationManager
import com.green.wallet.presentation.custom.convertDpToPixel
import com.green.wallet.presentation.main.send.TokenSpinnerAdapter
import com.green.wallet.presentation.tools.VLog
import com.green.wallet.presentation.tools.getMainActivity
import dagger.android.support.DaggerFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class ExchangeFragment : DaggerFragment() {

	private lateinit var binding: FragmentExchangeBinding

	@Inject
	lateinit var animManager: AnimationManager


	override fun onAttach(context: Context) {
		super.onAttach(context)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
	}


	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentExchangeBinding.inflate(layoutInflater)
		binding.registerViews()
		binding.registerFilters()
		binding.initSpinners()
		return binding.root
	}

	private fun FragmentExchangeBinding.registerViews() {

		initDetailTransaction(container, txtDetailTransactions, imgArrowDownDetailTrans)
		animManager.animateArrowIconCustomSpinner(
			binding.tokenToSpinner,
			imgArrowTo,
			getMainActivity()
		)
		animManager.animateArrowIconCustomSpinner(
			binding.tokenFromSpinner,
			imgArrowFrom,
			getMainActivity()
		)

		edtAmountFrom.addTextChangedListener {
			val amount = StringBuilder(it.toString())
			if (amount.toString().toDoubleOrNull() == null) {
				amount.append(0, '0')
			}
			val double = amount.toString().toDoubleOrNull() ?: 0.0
			constraintCommentMinAmount.visibility = if (double == 0.0) View.VISIBLE else View.GONE
		}

	}

	private fun FragmentExchangeBinding.initSpinners() {
		val tokenFromAdapter = TokenSpinnerAdapter(getMainActivity(), listOf("XCH", "USDT"))
		val tokenToAdapter = TokenSpinnerAdapter(getMainActivity(), listOf("XCH", "USDT"))
		tokenToSpinner.adapter = tokenToAdapter
		tokenFromSpinner.adapter = tokenFromAdapter
		edtTokenFrom.setOnClickListener {
			tokenFromSpinner.performClick()
		}
		imgArrowFrom.setOnClickListener {
			tokenFromSpinner.performClick()
		}
		edtTokenTo.setOnClickListener {
			tokenToSpinner.performClick()
		}
		imgArrowTo.setOnClickListener {
			tokenToSpinner.performClick()
		}
		tokenFromSpinner.onItemSelectedListener =
			object : AdapterView.OnItemSelectedListener {

				override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
					VLog.d("Selected item position : $p2 On From")
					tokenFromAdapter.selectedPosition = p2
					tokenToSpinner.setSelection(if (p2 == 0) 1 else 0)
					updateTokenTxtViews(tokenFromAdapter, tokenToAdapter)
				}

				override fun onNothingSelected(p0: AdapterView<*>?) {

				}

			}
		tokenToSpinner.setSelection(1)
		tokenToSpinner.onItemSelectedListener =
			object : AdapterView.OnItemSelectedListener {

				override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
					VLog.d("Selected item position : $p2 On To")
					tokenToAdapter.selectedPosition = p2
					tokenFromSpinner.setSelection(if (p2 == 0) 1 else 0)
					updateTokenTxtViews(tokenFromAdapter, tokenToAdapter)
				}

				override fun onNothingSelected(p0: AdapterView<*>?) {

				}

			}

		imgSwap.setOnClickListener {

		}

	}

	private fun updateTokenTxtViews(
		fromAdapter: TokenSpinnerAdapter,
		toAdapter: TokenSpinnerAdapter
	) {
		binding.apply {
			edtTokenFrom.text = fromAdapter.dataOptions[fromAdapter.selectedPosition]
			edtTokenTo.text = toAdapter.dataOptions[toAdapter.selectedPosition]
		}
	}

	fun changingOptionsOnSpinner(
		firstSpinner: Boolean,
		fromAdapter: TokenSpinnerAdapter,
		toAdapter: TokenSpinnerAdapter
	) {
		if (firstSpinner) {
			toAdapter.selectedPosition = if (fromAdapter.selectedPosition == 0) 1 else 0
			binding.edtTokenTo.text = toAdapter.dataOptions[toAdapter.selectedPosition]
		} else {
			fromAdapter.selectedPosition = if (toAdapter.selectedPosition == 0) 1 else 0
			binding.edtTokenFrom.text = fromAdapter.dataOptions[fromAdapter.selectedPosition]
		}
		binding.apply {
			edtFromNetwork.text =
				if (fromAdapter.selectedPosition == 0) "Chia Network" else "TRC-20"
			edtToNetwork.text = if (toAdapter.selectedPosition == 0) "Chia Network" else "TRC-20"
		}
	}

	var nextHeight = 418
	var containerBigger = true
	lateinit var anim: ValueAnimator

	private fun FragmentExchangeBinding.registerFilters() {
		val filterAmountEdtFrom = object : InputFilter {
			override fun filter(
				p0: CharSequence?,
				p1: Int,
				p2: Int,
				p3: Spanned?,
				p4: Int,
				p5: Int
			): CharSequence {
				if (p0 == null) return ""
				val curText = edtAmountFrom.text.toString()
				val locComo = curText.indexOf('.')
				val cursor = edtAmountFrom.selectionStart
				if (locComo == -1 || locComo == 0 || cursor <= locComo)
					return p0
				val digitsAfterComo = curText.substring(locComo + 1, curText.length).length
				if (digitsAfterComo >= 2) {
					return ""
				}
				VLog.d("Cur input amount from : $p0")
				return p0
			}
		}
		edtAmountFrom.filters = arrayOf(filterAmountEdtFrom)
	}

	private fun initDetailTransaction(layout: View, viewClick: View, arrow: View) {
		VLog.d("Change View Height : $nextHeight on ExchangeFragment")
		if (nextHeight == 300) {
			animManager.rotateBy180ForwardNoAnimation(
				binding.imgArrowDownDetailTrans,
				getMainActivity()
			)
			val params = binding.container.layoutParams
			params.height = getMainActivity().convertDpToPixel(418)
			binding.container.layoutParams = params
		}
		viewClick.setOnClickListener {
			initAnimationCollapsingDetailTransaction(layout)
		}
		arrow.setOnClickListener {
			initAnimationCollapsingDetailTransaction(layout)
		}
	}

	fun initAnimationCollapsingDetailTransaction(layout: View) {
		if (this::anim.isInitialized && anim.isRunning)
			return
		val newHeightPixel = (nextHeight * resources.displayMetrics.density).toInt()
		anim = ValueAnimator.ofInt(layout.height, newHeightPixel)
		anim.duration = 500 // duration in milliseconds
		anim.interpolator = DecelerateInterpolator()
		anim.addUpdateListener {
			val value = it.animatedValue as Int
			layout.layoutParams.height = value
			layout.requestLayout()
		}
		anim.start()
		if (containerBigger) {
			animManager.rotateBy180Forward(binding.imgArrowDownDetailTrans, getMainActivity())
		} else
			animManager.rotateBy180Backward(binding.imgArrowDownDetailTrans, getMainActivity())
		containerBigger = !containerBigger
		if (containerBigger) {
			nextHeight = 418
		} else {
			nextHeight = 300
		}
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

	}

}
