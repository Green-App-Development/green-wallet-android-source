package com.android.greenapp.presentation.main.managewallet

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager.widget.PagerAdapter
import com.android.greenapp.R
import com.android.greenapp.domain.entity.Wallet
import com.android.greenapp.presentation.custom.AnimationManager
import com.android.greenapp.presentation.custom.formattedDoubleAmountWithPrecision
import com.android.greenapp.presentation.custom.getShortNetworkType
import com.android.greenapp.presentation.main.MainActivity
import com.example.common.tools.VLog
import com.android.greenapp.presentation.tools.getStringResource

/**
 * Created by bekjan on 28.04.2022.
 * email: bekjan.omirzak98@gmail.com
 */

class ManageWalletViewPagerAdapter(
    val walletList: List<Wallet>,
    private val effect: AnimationManager,
    private val adapterListener: ManageWalletAdapterListener,
    private val activity: MainActivity
) :
    PagerAdapter() {

    private lateinit var layoutInflater: LayoutInflater


    val views = Array<View?>(walletList.size) { null }

    override fun getCount(): Int {
        return walletList.size
    }


    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return (view == `object` as ConstraintLayout)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        layoutInflater =
            container.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val manageWaleltView = layoutInflater.inflate(R.layout.item_manage_wallet, container, false)

        views[position] = manageWaleltView
        initViewDetails(manageWaleltView, walletList[position], position)

        manageWaleltView.findViewById<TextView>(R.id.txt_show_data).setOnClickListener {
            it.startAnimation(effect.getBtnEffectAnimation())
            adapterListener.showDataClicked()
        }

        manageWaleltView.apply {

            findViewById<ImageView>(R.id.ic_copy_green).setOnClickListener {
                adapterListener.imgCopyClicked(walletList[position].address)
            }

            findViewById<ImageView>(R.id.ic_copy_green_2).setOnClickListener {
                adapterListener.imgCopyClicked("${walletList[position].fingerPrint}")
            }

            findViewById<ImageView>(R.id.ic_copy_green_3).setOnClickListener {
                val mnemonics = walletList[position].mnemonics
                adapterListener.imgCopyClicked(getMnemonicsString(mnemonics))
            }

        }

        container.addView(manageWaleltView)
        return manageWaleltView
    }

    private fun getMnemonicsString(mnemonics: List<String>): String {
        var str = ""
        for (mne in mnemonics) {
            str += "$mne "
        }
        return str
    }

    @SuppressLint("SetTextI18n")
    private fun initViewDetails(view: View, wallet: Wallet, position: Int) {
        val edtAddress = view.findViewById<TextView>(R.id.edtAddress)
        val edtPublicKey = view.findViewById<TextView>(R.id.edtPublicKey)
        val fingerPrintWithPk = view.findViewById<TextView>(R.id.txtFingerPrintPk)
        val txtMyBalance = view.findViewById<TextView>(R.id.txtAmount)
        val txtAmountInUsd = view.findViewById<TextView>(R.id.txtAmountInUsd)
        val txtNetworkName = view.findViewById<TextView>(R.id.txtNetworkName)
        edtAddress.setText(wallet.address)
        edtPublicKey.setText("${wallet.fingerPrint}")
        fingerPrintWithPk.setText("${activity.getStringResource(R.string.private_key_with_public_fingerprint)}  ${wallet.fingerPrint}")
        txtMyBalance.setText(
            "${formattedDoubleAmountWithPrecision(walletList[position].balance)} ${
                getShortNetworkType(
                    wallet.networkType
                )
            }"
        )
        val formattedBalance = String.format("%.2f", wallet.balanceInUSD).replace(",", ".")
        val balance = "⁓$formattedBalance USD"
        txtAmountInUsd.setText(balance)
        txtNetworkName.setText(wallet.networkType)
    }

    fun changeToShowData(id: Int, show_data_visible: Boolean) {
        VLog.d("Change To Show Data View visibility : $show_data_visible")
        val view = views[id]
        view?.apply {
            view.findViewById<CardView>(R.id.card_view_balance).visibility =
                if (show_data_visible) View.GONE else View.VISIBLE
            view.findViewById<CardView>(R.id.card_view_show_data).visibility =
                if (show_data_visible) View.VISIBLE else View.GONE
        }
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as ConstraintLayout)
    }

    interface ManageWalletAdapterListener {
        fun showDataClicked()
        fun imgCopyClicked(data: String)
    }


}