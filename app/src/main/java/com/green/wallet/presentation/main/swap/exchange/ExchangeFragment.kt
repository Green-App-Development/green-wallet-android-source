package com.green.wallet.presentation.main.swap.exchange

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import com.green.wallet.databinding.FragmentExchangeBinding
import com.green.wallet.presentation.custom.AnimationManager
import com.green.wallet.presentation.main.send.TokenSpinnerAdapter
import com.green.wallet.presentation.tools.VLog
import com.green.wallet.presentation.tools.getMainActivity
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class ExchangeFragment : DaggerFragment() {

	private lateinit var binding: FragmentExchangeBinding

	@Inject
	lateinit var animManager: AnimationManager

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
		return binding.root
	}

	private fun FragmentExchangeBinding.registerViews() {

		initDetailTransaction(container, txtDetailTransactions)
		initDetailTransaction(container, imgArrowDownDetailTrans)
		initChooseTokenSpinnerFrom(edtTokenFrom, tokenFromSpinner)
		initChooseTokenSpinnerFrom(imgArrowFrom, tokenFromSpinner)
		initChooseTokenSpinnerTo(edtTokenTo, tokenToSpinner)
		initChooseTokenSpinnerTo(imgArrowTo, tokenToSpinner)
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
	}

	var nextHeight = 418
	var containerBigger = true
	lateinit var anim: ValueAnimator

	private fun initChooseTokenSpinnerFrom(viewClick: View, spinner: Spinner) {
		val tokenAdapter = TokenSpinnerAdapter(getMainActivity(), listOf("XCH", "XCC", "GAD"))
		spinner.adapter = tokenAdapter
		viewClick.setOnClickListener {
			spinner.performClick()
		}
		spinner.onItemSelectedListener =
			object : AdapterView.OnItemSelectedListener {

				override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
					VLog.d("Selected item position : $p2")
					tokenAdapter.selectedPosition = p2
					binding.edtTokenFrom.text = tokenAdapter.dataOptions[p2]
				}

				override fun onNothingSelected(p0: AdapterView<*>?) {

				}

			}
	}

	private fun initChooseTokenSpinnerTo(viewClick: View, spinner: Spinner) {
		val tokenAdapter = TokenSpinnerAdapter(getMainActivity(), listOf("XCH", "XCC", "GAD"))
		spinner.adapter = tokenAdapter
		viewClick.setOnClickListener {
			spinner.performClick()
		}
		spinner.onItemSelectedListener =
			object : AdapterView.OnItemSelectedListener {

				override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
					VLog.d("Selected item position : $p2")
					tokenAdapter.selectedPosition = p2
					binding.edtTokenTo.text = tokenAdapter.dataOptions[p2]
				}

				override fun onNothingSelected(p0: AdapterView<*>?) {

				}

			}
	}

	private fun initDetailTransaction(layout: View, viewClick: View) {
		viewClick.setOnClickListener {
			if (this::anim.isInitialized && anim.isRunning)
				return@setOnClickListener
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
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

	}

}
