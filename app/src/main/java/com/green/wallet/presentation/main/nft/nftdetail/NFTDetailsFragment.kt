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
import com.example.common.tools.VLog
import com.green.wallet.databinding.FragmentNftDetailBinding
import com.green.wallet.presentation.main.MainActivity
import dagger.android.support.DaggerFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by bekjan on 07.02.2023.
 * email: bekjan.omirzak98@gmail.com
 */
class NFTDetailsFragment : DaggerFragment() {

	private lateinit var binding: FragmentNftDetailBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

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

	}


	fun FragmentNftDetailBinding.registerClicks() {


//		scrollNFTProperties.setOnScrollChangeListener { view, curX, curY, oldX, oldY ->
//			if (curY > oldY) {
//				//down
//				val diff = Math.abs(curY - oldY)
//				if (diff >= 10)
//					moveRelativeLayoutToBtmSlowly(relativeLayoutBtnSend)
//			} else {
//				//up
//				val diff = Math.abs(curY - oldY)
//				if (diff >= 10)
//					moveRelativeLayoutFromBtmSlowly(relativeLayoutBtnSend)
//			}
//		}

		btnSend.setOnClickListener {
			curActivity().move2SendNFTFragment()
		}

		backLayout.setOnClickListener {
			curActivity().popBackStackOnce()
		}


		imgCpyDataUrl.setOnClickListener {
			copyToClipBoardShowCopied("Sample Copied")
		}

		imgCpyMetadataUrl.setOnClickListener {
			copyToClipBoardShowCopied("Sample Copied")
		}

		imgCpyNftId.setOnClickListener {
			copyToClipBoardShowCopied("Sample Copied")
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

	fun `moveRelativeLayoutFromBtmSlowly`(relLayout: RelativeLayout) {
		if (!isHiddenRelLayoutBtnSend)
			return
		isHiddenRelLayoutBtnSend = false
		val animator =
			ObjectAnimator.ofFloat(relLayout, "translationY", relLayout.height.toFloat(), 0f)
		animator.duration = 1000
		animator.start()
	}

	fun curActivity() = requireActivity() as MainActivity


}
