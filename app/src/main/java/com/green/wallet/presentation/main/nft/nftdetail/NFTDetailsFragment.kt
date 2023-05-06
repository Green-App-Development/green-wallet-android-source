package com.green.wallet.presentation.main.nft.nftdetail

import android.animation.ObjectAnimator
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.common.tools.formatString
import com.green.wallet.R
import com.green.wallet.presentation.tools.VLog
import com.green.wallet.databinding.FragmentNftDetailBinding
import com.green.wallet.domain.domainmodel.NFTInfo
import com.green.wallet.presentation.custom.convertDpToPixel
import com.green.wallet.presentation.main.MainActivity
import com.green.wallet.presentation.main.nft.usernfts.NFTTokenAdapter
import com.green.wallet.presentation.tools.getDrawableResource
import com.green.wallet.presentation.tools.getMainActivity
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_send_nft.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class NFTDetailsFragment : DaggerFragment() {

	private lateinit var binding: FragmentNftDetailBinding

	companion object {
		const val NFT_KEY = "nft_key"
	}

	lateinit var nftInfo: NFTInfo

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		arguments?.let {
			val nftInfo = it.getParcelable<NFTInfo>(NFT_KEY)
			this.nftInfo = nftInfo!!
			VLog.d("NFT Details on Fragment : $nftInfo")
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentNftDetailBinding.inflate(layoutInflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.registerClicks()
		binding.updateViews()
	}


	fun FragmentNftDetailBinding.registerClicks() {


		scrollNFTProperties.setOnScrollChangeListener { view, curX, curY, oldX, oldY ->
			if (curY > oldY) {
				//down
				val diff = Math.abs(curY - oldY)
				if (diff >= 10)
					moveRelativeLayoutToBtmSlowly(relativeLayoutBtnSend)
			} else {
				//up
				val diff = Math.abs(curY - oldY)
				if (diff >= 10)
					moveRelativeLayoutFromBtmSlowly(relativeLayoutBtnSend)
			}
		}

		btnSend.setOnClickListener {
			curActivity().move2SendNFTFragment(nftInfo)
		}

		backLayout.setOnClickListener {
			curActivity().popBackStackOnce()
		}


		imgCpyDataUrl.setOnClickListener {
			copyToClipBoardShowCopied(nftInfo.data_url)
		}

		imgCpyMetadataUrl.setOnClickListener {
			copyToClipBoardShowCopied(nftInfo.meta_url)
		}

		imgCpyNftId.setOnClickListener {
			copyToClipBoardShowCopied(nftInfo.nft_id)
		}

	}

	fun FragmentNftDetailBinding.updateViews() {
		VLog.d("Update views  on nft details fragment -> $nftInfo")
		initUpdatePropertiesRecView()
		edtNFTName.text = nftInfo.name
		edtNftDescription.text = nftInfo.description
		edtNftCollection.text = nftInfo.collection
		edtNFTID.text = formatString(8, nftInfo.nft_id, 4)
		edtLaunchedID.text = formatString(4, nftInfo.launcher_id, 4)
		edtOwnerID.text = "Unassigned"
		edtMinterDID.text = ""
		edtRoyaltyPercentage.text = "${nftInfo.royalty_percentage}%"
		edtMinterBlockHeight.text = "${nftInfo.mint_height}"
		edtDataUrl1.text = formatString(15, nftInfo.data_url, 0)
		edtDataHash.text = formatString(6, nftInfo.data_hash, 4)
		edtMetaData1.text = formatString(15, nftInfo.meta_url, 0)
		edtMetadataHash.text = formatString(6, nftInfo.data_hash, 4)
		Glide.with(getMainActivity()).load(nftInfo.data_url)
			.placeholder(getMainActivity().getDrawableResource(R.drawable.img_nft))
			.into(imgNft)
	}

	private fun initUpdatePropertiesRecView() {
		binding.apply {
			val adapter = NftPropertiesAdapter(nftInfo.properties)
			recViewPropertiesNft.layoutManager = GridLayoutManager(getMainActivity(), 2)
			recViewPropertiesNft.adapter = adapter
			adapter.updateNFTPropertyList(nftInfo.properties.keys.toList())
			val params = recViewPropertiesNft.layoutParams
			val len = nftInfo.properties.size
			params.height =
				getMainActivity().convertDpToPixel(78 * if (len % 2 == 0) len / 2 else len / 2 + 1)
			recViewPropertiesNft.layoutParams = params
		}
	}

	private fun copyToClipBoardShowCopied(text: String) {
		val clipBoard =
			curActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
		val clip = ClipData.newPlainText(
			"label",
			text
		)
		clipBoard.setPrimaryClip(clip)
		binding.relCopied.visibility = View.VISIBLE
		lifecycleScope.launch {
			delay(2000)
			kotlin.runCatching {
				binding.relCopied.visibility = View.GONE
			}.onFailure {
				VLog.d("Exception in changing relCopied visibility")
			}
		}
	}

	var isHiddenRelLayoutBtnSend = false

	fun moveRelativeLayoutToBtmSlowly(relLayout: RelativeLayout) {
		if (isHiddenRelLayoutBtnSend)
			return
		isHiddenRelLayoutBtnSend = true
		val animator =
			ObjectAnimator.ofFloat(relLayout, "translationY", 0f, relLayout.height.toFloat())
		animator.duration = 1000
		animator.start()
	}

	fun moveRelativeLayoutFromBtmSlowly(relLayout: RelativeLayout) {
		if (!isHiddenRelLayoutBtnSend)
			return
		isHiddenRelLayoutBtnSend = false
		val animator =
			ObjectAnimator.ofFloat(relLayout, "translationY", relLayout.height.toFloat(), 0f)
		animator.duration = 500
		animator.start()
	}


	fun curActivity() = requireActivity() as MainActivity


}
