package com.green.wallet.presentation.custom

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.green.wallet.R
import com.green.wallet.presentation.main.MainActivity
import com.green.wallet.presentation.tools.dpToPx
import com.green.wallet.presentation.tools.pxToDp


class DynamicSpinnerAdapter(
	private val widthDp: Int,
	private val context: Context,
	var dataOptions: List<String>
) : BaseAdapter() {

	var selectedPosition: Int = 0

	fun updateData(list: List<String>) {
		dataOptions = list
		notifyDataSetChanged()
	}

	override fun getCount(): Int {
		return dataOptions.size
	}

	override fun getItem(p0: Int): Any {
		return dataOptions[p0]
	}

	override fun getItemId(p0: Int): Long {
		return 0
	}

	@SuppressLint("ViewHolder", "CutPasteId")
	override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
		val rootView = LayoutInflater.from(context)
			.inflate(R.layout.item_adapter_gen, p2, false)
		val layoutParams = LinearLayout.LayoutParams(
			context.dpToPx(widthDp),
			LinearLayout.LayoutParams.WRAP_CONTENT
		)
		rootView.layoutParams = layoutParams
		val txtNameNetwork = rootView.findViewById<TextView>(R.id.txtNetworkName)
		val relBcgNetworkType = rootView.findViewById<RelativeLayout>(R.id.bcgNetworkType)
		txtNameNetwork.text = dataOptions[p0]
		if (p0 == selectedPosition) {
			txtNameNetwork
				.setTextColor(
					ContextCompat.getColor(
						context,
						R.color.checked_spinner_text_color
					)
				)
			relBcgNetworkType.setBackgroundColor(
				ContextCompat.getColor(
					context,
					R.color.checked_spinner_bcg_color
				)
			)
		} else {
			txtNameNetwork
				.setTextColor(
					ContextCompat.getColor(
						context,
						R.color.unchecked_spinner_text_color
					)
				)
			relBcgNetworkType.setBackgroundColor(
				ContextCompat.getColor(
					context,
					R.color.unchecked_spinner_bcg_color
				)
			)
		}
		return rootView
	}


}
