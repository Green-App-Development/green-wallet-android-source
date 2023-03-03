package com.green.wallet.presentation.main.nft.usernfts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.green.wallet.R
import com.green.wallet.databinding.FragmentUserNftBinding
import com.green.wallet.domain.domainmodel.NFTToken
import com.green.wallet.presentation.custom.AnimationManager
import com.green.wallet.presentation.custom.convertPixelToDp
import com.green.wallet.presentation.main.MainActivity
import com.green.wallet.presentation.main.send.NetworkAdapter
import com.green.wallet.presentation.tools.getStringResource
import dagger.android.support.DaggerFragment
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


class UserNFTTokensFragment : DaggerFragment(), NFTTokenAdapter.NFTTokenClicked {

	private lateinit var binding: FragmentUserNftBinding

	@Inject
	lateinit var animManager: AnimationManager

	private val nftAdapter by lazy {
		NFTTokenAdapter(this)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentUserNftBinding.inflate(layoutInflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.registerClicks()
		updateViewDetails()

	}

	private fun dummyNFTData() {
		binding.apply {
			btnExploreMarkets.visibility = View.GONE
//			placeHolderNoNFTs.visibility = View.GONE
			txtNoNFTPlaceHolder.visibility = View.GONE
			constraintCommentExploreMarkets.visibility = View.GONE

			val data = mutableListOf<NFTToken>()
			(1..12).forEach {
				data.add(
					NFTToken(
						R.drawable.img_duck,
						"GAD NFT Collection #$it",
						"GAD NFT collection"
					)
				)
			}

			recViewNft.layoutManager = GridLayoutManager(curActivity(), 2)
			recViewNft.setHasFixedSize(true)
			recViewNft.adapter = nftAdapter
			nftAdapter.updateNFTTokenList(data)

		}
	}

	private fun updateViewDetails() {
		//nftAdapter
		val nftAdapter =
			NetworkAdapter(
				curActivity(),
				listOf(curActivity().getStringResource(R.string.all_nfts))
			)
		binding.nftTypeSpinner.adapter = nftAdapter


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
			dummyNFTData()
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

	override fun onNFTToken() {
		curActivity().move2NFTDetailsFragment()
	}


}
