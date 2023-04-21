package com.green.wallet.presentation.main.swap.requestdetail

import android.os.Bundle
import android.text.InputFilter
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import com.example.common.tools.convertStringToRequestStatus
import com.example.common.tools.getRequestStatusColor
import com.example.common.tools.getRequestStatusTranslation
import com.green.wallet.R
import com.green.wallet.databinding.FragmentRequestDetailsBinding
import com.green.wallet.presentation.tools.*
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_exchange.*
import kotlinx.android.synthetic.main.fragment_send.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RequestDetailFragment : DaggerFragment() {


	companion object {
		const val KEY_ID = "key_id"
	}

	private var status = RequestStatus.InProgress

	private lateinit var binding: FragmentRequestDetailsBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		VLog.d("Arguments on request Detail : $arguments")
		status = convertStringToRequestStatus(
			arguments?.getString(KEY_ID) ?: RequestStatus.InProgress.name
		)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentRequestDetailsBinding.inflate(layoutInflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		if (status == RequestStatus.Cancelled) {
			statusCancelled()
		}
		binding.initUpdateRequestItemViews()
		binding.registerClicks()
	}

	private fun FragmentRequestDetailsBinding.registerClicks() {

		backLayout.setOnClickListener {
			getMainActivity().popBackStackOnce()
		}

		btnPay.setOnClickListener {
			getMainActivity().move2BtmDialogPayment("", 0.003)
		}

	}


	private fun FragmentRequestDetailsBinding.initUpdateRequestItemViews() {
		txtStatus.text = "${getMainActivity().getStringResource(R.string.status_title)}: ${
			getRequestStatusTranslation(
				getMainActivity(),
				status
			)
		}"
		txtRequestHash.text = getMainActivity().getStringResource(R.string.order_title) + " #001766"
		changeColorTxtStatusRequest(txtStatus, getRequestStatusColor(status, getMainActivity()))
		val params = scrollViewProperties.layoutParams as ConstraintLayout.LayoutParams
		when (status) {
			RequestStatus.InProgress -> {
				layoutInProgress.visibility = View.VISIBLE
				params.bottomToTop = R.id.layout_in_progress
				startTempTimer(edtFinishTranTime)
			}
			RequestStatus.Waiting -> {
				layoutWaiting.visibility = View.VISIBLE
				params.bottomToTop = R.id.layout_waiting
				startTempTimer(edtAutoCancelTime)
			}
			else -> {
				params.bottomToBottom = R.id.root
			}
		}
		scrollViewProperties.layoutParams = params
		txtAutoCancel.text = "${getMainActivity().getStringResource(R.string.auto_cancel)}:"

		if (status == RequestStatus.Waiting)
			txtSent.text = getMainActivity().getStringResource(R.string.need_to_send)
		if (status == RequestStatus.InProgress || status == RequestStatus.Waiting) {
			txtReceive.text = getMainActivity().getStringResource(R.string.you_will_receive)
		}

		txtFinishTran.text =
			getMainActivity().getStringResource(R.string.completion_oper_flow) + ":"

	}


	fun startTempTimer(txt: TextView) {
		lifecycleScope.launch {
			var totalSeconds = 60 * 15
			while (totalSeconds >= 0) {
				val minutes = totalSeconds / 60
				val seconds = totalSeconds % 60
				txt.text = "${addZeroToFrontIfNeeded(minutes)}:${addZeroToFrontIfNeeded(seconds)}"
				totalSeconds--
				delay(1000)
			}
		}
	}

	fun addZeroToFrontIfNeeded(num: Int): String {
		val str = "$num"
		if (str.length == 2)
			return str
		return "0$str"
	}

	private fun statusCancelled() {
		binding.apply {
			listOf(imgCpyAddress, imgCpyReceiveAddress, imgCpyHashTransaction).forEach {
				it.visibility = View.GONE
			}
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


}
