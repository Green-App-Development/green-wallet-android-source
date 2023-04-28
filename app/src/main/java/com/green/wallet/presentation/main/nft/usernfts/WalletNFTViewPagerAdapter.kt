package com.green.wallet.presentation.main.nft.usernfts

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager.widget.PagerAdapter
import com.green.wallet.databinding.ItemWalletNftViewPagerBinding

class WalletNFTViewPagerAdapter(
	private val activity: Activity,

) : PagerAdapter() {


	lateinit var layoutInflater: LayoutInflater

	private val views = Array<View?>(10) { null }

	override fun getCount(): Int {
		return 10
	}

	override fun isViewFromObject(view: View, `object`: Any): Boolean {
		return (view == `object` as ConstraintLayout)
	}

	override fun instantiateItem(container: ViewGroup, position: Int): Any {
		layoutInflater =
			activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
		val viewPagerItem = ItemWalletNftViewPagerBinding.inflate(layoutInflater).root
		registerViews()
		container.addView(viewPagerItem)
		views[position] = viewPagerItem
		return views[position]!!
	}

	private fun registerViews() {

	}


}
