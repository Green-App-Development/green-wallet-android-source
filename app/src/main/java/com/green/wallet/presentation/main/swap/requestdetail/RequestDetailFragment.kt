package com.green.wallet.presentation.main.swap.requestdetail

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.common.tools.convertStringToRequestStatus
import com.example.common.tools.getRequestStatusColor
import com.example.common.tools.getRequestStatusTranslation
import com.green.wallet.R
import com.green.wallet.databinding.FragmentRequestDetailsBinding
import com.green.wallet.presentation.tools.RequestStatus
import com.green.wallet.presentation.tools.VLog
import com.green.wallet.presentation.tools.getColorResource
import com.green.wallet.presentation.tools.getMainActivity
import dagger.android.support.DaggerFragment

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
	}

	private fun FragmentRequestDetailsBinding.initUpdateRequestItemViews() {
		txtStatus.text = "Статус: ${getRequestStatusTranslation(status)}"
		changeColorTxtStatusRequest(txtStatus, getRequestStatusColor(status, getMainActivity()))
		val params = scrollViewProperties.layoutParams as ConstraintLayout.LayoutParams
		when (status) {
			RequestStatus.InProgress -> {
				layoutInProgress.visibility = View.VISIBLE
				params.bottomToTop = R.id.layout_in_progress
			}
			RequestStatus.Waiting -> {
				layoutWaiting.visibility = View.VISIBLE
				params.bottomToTop = R.id.layout_waiting
			}
			else -> {
				params.bottomToBottom = R.id.root
			}
		}
		scrollViewProperties.layoutParams = params
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
