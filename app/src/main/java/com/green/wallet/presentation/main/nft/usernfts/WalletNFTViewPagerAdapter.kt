package com.green.wallet.presentation.main.nft.usernfts

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager.widget.PagerAdapter
import com.green.wallet.databinding.ItemWalletNftViewPagerBinding
import com.green.wallet.domain.domainmodel.NFTInfo
import com.green.wallet.domain.domainmodel.WalletWithNFTInfo
import com.green.wallet.presentation.custom.getNetworkFromAddress
import com.green.wallet.presentation.custom.hidePublicKey
import com.green.wallet.presentation.main.MainActivity
import com.green.wallet.presentation.tools.VLog

class WalletNFTViewPagerAdapter(
	private val activity: MainActivity,
	private val walletList: List<WalletWithNFTInfo>
) : PagerAdapter(), NFTTokenAdapter.NFTTokenClicked {


	lateinit var layoutInflater: LayoutInflater

	private val views = Array<View?>(walletList.size) { null }

	override fun getCount(): Int {
		return walletList.size
	}

	override fun isViewFromObject(view: View, `object`: Any): Boolean {
		return (view == `object` as ConstraintLayout)
	}

	override fun instantiateItem(container: ViewGroup, position: Int): Any {
		layoutInflater =
			activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
		val binding = ItemWalletNftViewPagerBinding.inflate(layoutInflater)
		val viewPagerItem = binding.root
		binding.registerViews(position)
		container.addView(viewPagerItem)
		views[position] = viewPagerItem
		return views[position]!!
	}

	private fun ItemWalletNftViewPagerBinding.registerViews(position: Int) {
		val nftWallet = walletList[position]
		VLog.d("NFTWalletViewPager register views pos : $position and mapSize : ${nftWallet.nftInfos.size}")
		if (nftWallet.nftInfos.isNotEmpty()) {
			btnExploreMarkets.visibility = View.GONE
			txtNoNFTPlaceHolder.visibility = View.GONE
			linearDummyNftImg1.visibility = View.GONE
			linearDummyNftImg2.visibility = View.GONE
			recViewNft.visibility = View.VISIBLE
			val nftAdapter = NFTTokenAdapter(this@WalletNFTViewPagerAdapter, activity)
			recViewNft.adapter = nftAdapter
			recViewNft.layoutManager = GridLayoutManager(activity, 2)
			nftAdapter.updateNFTTokenList(nftWallet.nftInfos)
		} else {
			recViewNft.visibility = View.GONE
			btnExploreMarkets.visibility = View.VISIBLE
			txtNoNFTPlaceHolder.visibility = View.VISIBLE
			linearDummyNftImg1.visibility = View.VISIBLE
			linearDummyNftImg2.visibility = View.VISIBLE
			btnExploreMarkets.setOnClickListener {
				commentUnderDev.visibility = View.VISIBLE
				Handler(Looper.getMainLooper()).postDelayed({
					commentUnderDev.visibility = View.GONE
					it.isEnabled = true
				}, 2000L)
			}
		}
		txtNetwork.text = getNetworkFromAddress(nftWallet.address)
		txtHiddenPublicKey.text = hidePublicKey(nftWallet.fingerPrint)


	}

	override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
		container.removeView(`object` as ConstraintLayout)
	}

	override fun onNFTToken(nft: NFTInfo) {
		activity.move2NFTDetailsFragment(nft)
	}


}
