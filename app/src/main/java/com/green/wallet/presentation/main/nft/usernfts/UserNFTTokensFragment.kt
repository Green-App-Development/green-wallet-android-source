package com.green.wallet.presentation.main.nft.usernfts

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.green.wallet.R
import com.green.wallet.databinding.FragmentUserNftBinding
import com.green.wallet.domain.domainmodel.WalletWithNFTInfo
import com.green.wallet.presentation.custom.AnimationManager
import com.green.wallet.presentation.custom.ConnectionLiveData
import com.green.wallet.presentation.custom.DialogManager
import com.green.wallet.presentation.custom.DynamicSpinnerAdapter
import com.green.wallet.presentation.custom.ViewPagerPosition
import com.green.wallet.presentation.custom.convertPixelToDp
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.main.MainActivity
import com.green.wallet.presentation.tools.VLog
import com.green.wallet.presentation.tools.getMainActivity
import com.green.wallet.presentation.tools.getStringResource
import com.green.wallet.presentation.viewBinding
import dagger.android.support.DaggerFragment
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserNFTTokensFragment : DaggerFragment() {

	private val binding: FragmentUserNftBinding by viewBinding(FragmentUserNftBinding::bind)

	@Inject
	lateinit var animManager: AnimationManager

	@Inject
	lateinit var viewPagerState: ViewPagerPosition

	@Inject
	lateinit var factory: ViewModelFactory
	private val vm: UserNFTTokensViewModel by viewModels { factory }
	private var prevChooseItemPosViewPager = 0

	@Inject
	lateinit var connectionLiveData: ConnectionLiveData

	@Inject
	lateinit var dialogMan: DialogManager

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		return inflater.inflate(R.layout.fragment_user_nft, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		VLog.d("View Created on UserNFT : $viewPagerState Pos : ${viewPagerState.pagerPosition}")
		binding.registerClicks()
		updateViewDetails()
		initViewPager()

	}

	private fun initViewPager() {
		kotlin.runCatching {
			lifecycleScope.launch {
				repeatOnLifecycle(Lifecycle.State.STARTED) {
					vm.getHomeAddedWalletWithNFT().collect {
						VLog.d("Retrieving walletListWithNFTAndCoins : $it")
						initNFTUpdateViewPager(it)
					}
				}
			}
		}.onFailure {
			VLog.d("Exception in nft view pager adapter : ${it.message}")
		}
	}

	private fun initNFTUpdateViewPager(walletList: List<WalletWithNFTInfo>) {
		val nftViewPagerAdapter = WalletNFTViewPagerAdapter(getMainActivity(), walletList)
		if (prevChooseItemPosViewPager >= walletList.size) {
			prevChooseItemPosViewPager = 0
			viewPagerState.pagerPosition = 0
		}
		binding.apply {
			nftViewPager.adapter = nftViewPagerAdapter
			nftViewPager.setCurrentItem(viewPagerState.pagerPosition, true)
			pageIndicator.count = walletList.size
			pageIndicator.setSelected(viewPagerState.pagerPosition)
			nftViewPager.addOnPageChangeListener(object : OnPageChangeListener {
				override fun onPageScrolled(
					position: Int,
					positionOffset: Float,
					positionOffsetPixels: Int
				) {

				}

				override fun onPageSelected(position: Int) {
					pageIndicator.setSelected(position)
					prevChooseItemPosViewPager = position
					viewPagerState.pagerPosition = position
				}

				override fun onPageScrollStateChanged(state: Int) {

				}

			})
			val rect = Rect()
			nftViewPager.getGlobalVisibleRect(rect)
			swipeRefreshNft.topY = rect.top + 35
			swipeRefreshNft.bottomY = rect.bottom
		}
	}

	private fun updateViewDetails() {
		//nftAdapter
		val nftAdapter =
			DynamicSpinnerAdapter(
				150,
				curActivity(),
				mutableListOf(curActivity().getStringResource(R.string.all_nfts))
			)
		binding.nftTypeSpinner.adapter = nftAdapter


		//networkAdapter
//		val networkAdapter = DynamicSpinnerAdapter(
//			165,
//			curActivity(),
//			listOf()
//		)

	}

	private var jobUnderDev: Job? = null

	private fun curActivity() = requireActivity() as MainActivity


	fun FragmentUserNftBinding.registerClicks() {

		linearNftTypes.setOnClickListener {
			nftTypeSpinner.performClick()
		}


		animManager.animateArrowIconCustomSpinner(
			nftTypeSpinner,
			icDownNFT,
			curActivity()
		)

		btnExploreMarkets.setOnClickListener {

		}

		swipeRefreshNft.apply {
			setOnRefreshListener {
				VLog.d("Is Online ${connectionLiveData.isOnline}")
				if (connectionLiveData.isOnline) {
					vm.swipedRefreshClicked {
						if (this@UserNFTTokensFragment.isVisible) {
							isRefreshing = false
						}
					}
				} else {
					isRefreshing = false
					dialogMan.showNoInternetTimeOutExceptionDialog(curActivity()) {

					}
				}
			}
			setColorSchemeResources(R.color.green)
			isNFTScreen = true
		}


	}

	fun onCommentUnderDevClicked() {
		if (jobUnderDev?.isActive == true) return
		jobUnderDev = lifecycleScope.launch {
			val totalWidthInDp =
				curActivity().convertPixelToDp(binding.root.width.toFloat())
			val lenItem = totalWidthInDp / 3
			val params = binding.dotConstraintComment.layoutParams as ViewGroup.MarginLayoutParams
			params.setMargins(0, 0, lenItem, 0)
			binding.dotConstraintComment.layoutParams = params
			binding.constraintBtmNavViewComment.visibility = View.VISIBLE
			delay(2000)
			binding.constraintBtmNavViewComment.visibility = View.GONE
		}
	}

}
