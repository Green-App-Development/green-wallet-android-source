package com.android.greenapp.presentation.main.send

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.android.greenapp.R
import com.android.greenapp.domain.entity.TokenWallet
import com.android.greenapp.domain.entity.Wallet
import com.android.greenapp.domain.entity.WalletWithTokens
import com.example.common.tools.VLog

/**
 * Created by bekjan on 18.05.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class WalletListAdapter(private val context: Context, val walletList: List<WalletWithTokens>) :
    BaseAdapter() {

    var selectedPosition: Int = 0

    override fun getCount(): Int {
        return walletList.size
    }

    override fun getItem(p0: Int): Any {
        return walletList[p0]
    }

    override fun getItemId(p0: Int): Long {
        return 0
    }

    @SuppressLint("ViewHolder")
    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val rootView = LayoutInflater.from(context).inflate(R.layout.item_adapter_wallet, p2, false)
        val txtCoinName = rootView.findViewById<TextView>(R.id.txt_network_name)
        val txtPublicKey = rootView.findViewById<TextView>(R.id.txtHiddenPublicKey)
        val relBcgNetworkType = rootView.findViewById<RelativeLayout>(R.id.bcgNetworkType)
        if (p0 == selectedPosition) {
            txtCoinName
                .setTextColor(ContextCompat.getColor(context, R.color.checked_spinner_text_color))
            txtPublicKey
                .setTextColor(ContextCompat.getColor(context, R.color.checked_spinner_text_color))
            relBcgNetworkType.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.checked_spinner_bcg_color
                )
            )
        } else {
            txtCoinName
                .setTextColor(ContextCompat.getColor(context, R.color.unchecked_spinner_text_color))
            txtPublicKey
                .setTextColor(ContextCompat.getColor(context, R.color.txt_greey))
            relBcgNetworkType.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.unchecked_spinner_bcg_color
                )
            )
        }

        kotlin.runCatching {
            txtCoinName.text = walletList[p0].networkType.split(" ")[0]
            txtPublicKey.text = walletList[p0].fingerPrint.toString()
        }.onFailure {
            VLog.d("Failed in setting wallet list adapter item detais  : $it")
        }

        return rootView
    }
}
