package com.green.wallet.presentation.main.swap.requestdetail

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.green.wallet.R
import com.green.wallet.databinding.FragmentRequestDetailsBinding
import com.green.wallet.presentation.tools.getColorResource
import com.green.wallet.presentation.tools.getMainActivity
import dagger.android.support.DaggerFragment

class RequestDetailFragment : DaggerFragment() {

	private lateinit var binding: FragmentRequestDetailsBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
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

	}

	fun changeAmountColor(txt: TextView, clr: Int) {
		val text = txt.text.toString()
		val pivot = text.indexOf(":")
		val spannableString = SpannableString(text)
		val textPart =
			ForegroundColorSpan(getMainActivity().getColorResource(R.color.sorting_txt_category))
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
