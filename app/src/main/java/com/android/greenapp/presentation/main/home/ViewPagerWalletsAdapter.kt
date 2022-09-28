package com.android.greenapp.presentation.main.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import com.android.greenapp.R
import com.android.greenapp.domain.entity.WalletWithTokens
import com.android.greenapp.presentation.custom.AnimationManager
import com.android.greenapp.presentation.custom.hidePublicKey
import com.android.greenapp.presentation.custom.trimNetwork

/**
 * Created by bekjan on 19.04.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class ViewPagerWalletsAdapter(
    private val activity: Activity,
    private val viewPagerClicker: ViewPagerWalletClicker,
    private val effect: AnimationManager,
    val walletList: List<WalletWithTokens>,
    val balanceIsHidden: Boolean
) : PagerAdapter() {

    lateinit var layoutInflater: LayoutInflater

    private val views = Array<View?>(walletList.size) { null }

    override fun getCount(): Int {
        return walletList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return (view == `object` as RelativeLayout)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        layoutInflater =
            activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val viewPagerItem = layoutInflater.inflate(R.layout.item_home_view_pager, container, false)
        registerClicks(viewPagerItem)
        initViewDetails(viewPagerItem, position)
        container.addView(viewPagerItem)
        views[position] = viewPagerItem
        return views[position]!!
    }

    @SuppressLint("SetTextI18n")
    private fun initViewDetails(viewPagerItem: View, position: Int) {
        val tokenRecView = viewPagerItem.findViewById<RecyclerView>(R.id.rec_view_wallet_tokens)
        val viewPagerTokenAdapter = WalletTokenAdapter(
            walletList[position].tokenWalletList,
            object : WalletTokenAdapter.TokenClicker {
                override fun onImportTokenClicked() {
                    viewPagerClicker.impToken(walletList[position].fingerPrint,walletList[position].main_puzzle_hash)
                }
            },
            activity,
            balanceIsHidden
        )
        tokenRecView.adapter = viewPagerTokenAdapter

        val txtView = viewPagerItem.findViewById<TextView>(R.id.txtWalletPublicKey)

        txtView.setText(
            "${trimNetwork(walletList[position].networkType)}     ${
                hidePublicKey(
                    walletList[position].fingerPrint
                )
            }"
        )
    }

    private fun registerClicks(view: View?) {
        if (view == null) return

        view.findViewById<TextView>(R.id.txtAllWallet).setOnClickListener {
            viewPagerClicker.allWallet()
        }

        view.findViewById<ImageView>(R.id.imgAddWallet).setOnClickListener {
            viewPagerClicker.addWallet()
        }
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as RelativeLayout)
    }

    interface ViewPagerWalletClicker {
        fun addWallet()
        fun allWallet()
        fun impToken(fingerPrint: Long,main_puzzle_hash:String)
    }


}
