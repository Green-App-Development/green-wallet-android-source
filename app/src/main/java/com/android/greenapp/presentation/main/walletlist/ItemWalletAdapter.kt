package com.android.greenapp.presentation.main.walletlist

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.android.greenapp.R
import com.android.greenapp.domain.entity.Wallet
import com.android.greenapp.presentation.custom.AnimationManager
import com.android.greenapp.presentation.custom.getShortNetworkType
import com.android.greenapp.presentation.tools.getStringResource
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.example.common.tools.VLog


class ItemWalletAdapter(
    private val activity: Activity,
    private val effect: AnimationManager,
    private val scope: LifecycleCoroutineScope
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val walletList = mutableListOf<Wallet>()
    lateinit var editLayoutListener: DetailLayoutListener
    private val viewBinderHelper = ViewBinderHelper()

    fun walletList(list: List<Wallet>) {
        walletList.clear()
        walletList.addAll(list)
        notifyDataSetChanged()
    }

    fun settingDetailLayoutListener(listener: DetailLayoutListener) {
        this.editLayoutListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_wallet, parent, false)
            WalletViewHolder(view)
        } else {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_add_wallet, parent, false)
            AddWalletViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            0 -> {
                holder as WalletViewHolder
                VLog.d("ViewType : ${holder.itemViewType}")
                holder.onBind(walletList[position])
                viewBinderHelper.setOpenOnlyOne(true)
                viewBinderHelper.bind(holder.swipeRevealLayout, "$position")
            }
            1 -> {
                holder as AddWalletViewHolder
                holder.addListener()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < walletList.size) 0 else 1
    }

    override fun getItemCount() = walletList.size + 1

    inner class WalletViewHolder(val v: View) : RecyclerView.ViewHolder(v) {

        val coinName: TextView = v.findViewById(R.id.txtNameCoin)
        val belowCoinName = v.findViewById<TextView>(R.id.belowName)
        val ic_delete = v.findViewById<ImageView>(R.id.ic_delete)
        var ic_star = v.findViewById<ImageView>(R.id.ic_star)
        val swipeRevealLayout = v.findViewById<SwipeRevealLayout>(R.id.root_swipe)
        val rootWalletDetail = v.findViewById<RelativeLayout>(R.id.rel_icon_detail)
        val txtWalletPublicKey = v.findViewById<TextView>(R.id.txtPublicKey)


        @SuppressLint("SetTextI18n")
        fun onBind(wallet: Wallet) {

            coinName.text = wallet.networkType.split(" ")[0]
            belowCoinName.text = getShortNetworkType(wallet.networkType)

            ic_star.setImageResource(if (wallet.home_id_added > 0L) R.drawable.ic_star_enabled else R.drawable.ic_star_not_enabled)

            txtWalletPublicKey.text =
                "${activity.getStringResource(R.string.private_key_with_public_fingerprint)} ${wallet.fingerPrint}"

            if (this@ItemWalletAdapter::editLayoutListener.isInitialized) {

                ic_star.setOnClickListener {
                    editLayoutListener.walletHomeAdded(wallet, ic_star) {

                    }
                    viewBinderHelper.closeLayout("$adapterPosition")
                }

                ic_delete.setOnClickListener {
                    it.startAnimation(effect.getBtnEffectAnimation())
                    editLayoutListener.deleteClicked(wallet)
                }

                rootWalletDetail.setOnClickListener {
                    VLog.d("rootWalletDetail Clicked Wallet : ${walletList[adapterPosition]}")
//                    it.startAnimation(effect.getBtnEffectAnimation())
                    editLayoutListener.onWalletItemClicked(adapterPosition)
                }

            }

        }

    }

    inner class AddWalletViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val addWalletLayout = v.findViewById<RelativeLayout>(R.id.rel_add_wallet)

        fun addListener() {
            addWalletLayout.setOnClickListener {
                VLog.d("Add Wallet Layout clicked ")
                if (this@ItemWalletAdapter::editLayoutListener.isInitialized) {
                    editLayoutListener.addWallet()
                }
            }
        }

    }

    interface DetailLayoutListener {
        fun walletHomeAdded(wallet: Wallet, imageView: ImageView, callBack: () -> Unit)
        fun deleteClicked(wallet: Wallet)
        fun onWalletItemClicked(itemId: Int)
        fun addWallet()
    }


}
