package com.green.wallet.presentation.main.transaction

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.green.wallet.R


class TransactionSortingAdapter(private val context: Context, val options: List<String>) : BaseAdapter() {

    var selectedPosition: Int = 0

    override fun getCount(): Int {
        return options.size
    }

    override fun getItem(p0: Int): Any {
        return options[p0]
    }

    override fun getItemId(p0: Int): Long {
        return 0
    }

    @SuppressLint("ViewHolder", "CutPasteId")
    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val rootView = LayoutInflater.from(context).inflate(R.layout.item_adapter_gen, p2, false)
        val txtNameNetwork = rootView.findViewById<TextView>(R.id.txtNetworkName)
        val relBcgNetworkType = rootView.findViewById<RelativeLayout>(R.id.bcgNetworkType)
        txtNameNetwork.text = options[p0]
        if (p0 == selectedPosition) {
            txtNameNetwork
                .setTextColor(ContextCompat.getColor(context, R.color.checked_spinner_text_color))
            relBcgNetworkType.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.checked_spinner_bcg_color
                )
            )
        } else {
            txtNameNetwork
                .setTextColor(ContextCompat.getColor(context, R.color.unchecked_spinner_text_color))
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
