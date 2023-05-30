package com.green.wallet.presentation.main.swap.qrsend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.green.wallet.R
import com.green.wallet.databinding.FragmentQrcodeSendBinding
import com.green.wallet.presentation.tools.VLog
import com.green.wallet.presentation.tools.getMainActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FragmentQRSend : Fragment() {

	private lateinit var binding: FragmentQrcodeSendBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentQrcodeSendBinding.inflate(layoutInflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.initViews()
	}

	private fun FragmentQrcodeSendBinding.initViews() {
		try {
			val qrgEncoder =
				QRGEncoder("Hello", null, QRGContents.Type.TEXT, 1500)
			qrgEncoder.colorBlack =
				ContextCompat.getColor(getMainActivity(), R.color.qr_black)
			qrgEncoder.colorWhite =
				ContextCompat.getColor(getMainActivity(), R.color.qr_white)
			val bitMap = qrgEncoder.bitmap
			qrImg.setImageBitmap(bitMap)
		} catch (ex: Exception) {
			VLog.e("Exception in creating bitmap : ${ex.message}")
		}

		backLayout.setOnClickListener {
			getMainActivity().popBackStackOnce()
		}

		relCopy.setOnClickListener {
			changeVisibleRelCopiedView()
		}

		imgCpySendAmount.setOnClickListener {
			changeVisibleRelCopiedView()
		}

		btnShare.setOnClickListener {
			binding.btnShare.setOnClickListener {
				getMainActivity().launchingIntentForSendingWalletAddress(binding.txtAddressWallet.text.toString())
			}
		}

	}

	private var job: Job? = null

	fun changeVisibleRelCopiedView() {
		binding.relCopied.visibility = View.VISIBLE
		job?.cancel()
		job = lifecycleScope.launch {
			delay(2000L)
			binding.relCopied.visibility = View.GONE
		}
	}

	override fun onStop() {
		super.onStop()
		job?.cancel()
	}


}


