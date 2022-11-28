package com.android.greenapp.presentation.main.scan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.android.greenapp.R
import com.android.greenapp.databinding.FragmentScannerBinding
import com.android.greenapp.presentation.di.factory.ViewModelFactory
import com.android.greenapp.presentation.main.MainActivity
import com.android.greenapp.presentation.main.send.SendFragmentViewModel
import com.android.greenapp.presentation.viewBinding
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.example.common.tools.VLog
import dagger.android.support.DaggerFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by bekjan on 22.04.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class ScannerFragment : DaggerFragment() {

	private val binding by viewBinding(FragmentScannerBinding::bind)
	private lateinit var codeScanner: CodeScanner

	@Inject
	lateinit var viewModelFactory: ViewModelFactory
	private val sendFragmentViewModel: SendFragmentViewModel by viewModels { viewModelFactory }


	companion object {
		const val FINGERPRINT_KEY = "finger_print_key"
		const val NETWORK_TYPE_KEY = "network_type_key"
	}

	private var curFingerPrint: Long? = null
	private var curNetworkType: String? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		arguments?.let {
			curFingerPrint = it.getLong(FINGERPRINT_KEY)
			if (it.getString(NETWORK_TYPE_KEY) != null) {
				curNetworkType = it.getString(NETWORK_TYPE_KEY)
			}
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		return inflater.inflate(R.layout.fragment_scanner, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		codeScanner()
		registerBtnListener()
	}


	private fun registerBtnListener() {
		binding.backLayout.setOnClickListener {
			it.startAnimation(animFadeInBackBtn)
			curActivity().popBackStackOnce()
		}

	}

	private fun codeScanner() {
		codeScanner = CodeScanner(curActivity(), binding.scannerView)
		codeScanner.apply {
			camera = CodeScanner.CAMERA_BACK
			formats = CodeScanner.ALL_FORMATS

			autoFocusMode = AutoFocusMode.SAFE
			scanMode = ScanMode.SINGLE
			isAutoFocusEnabled = true
			isFlashEnabled = false

			decodeCallback = DecodeCallback {
				if (it.text.isNotEmpty()) {
					lifecycleScope.launch(Dispatchers.Main) {
						determineDirection()
						delay(100)
						curActivity().mainViewModel.saveDecodeQrCode(it.text)
					}
				}
			}

			errorCallback = ErrorCallback {
				VLog.d("Code is not valid : ${it.message}")
			}

		}
		codeScanner.startPreview()

	}

	private fun determineDirection() {
		if (curFingerPrint == null || curNetworkType == null)
			curActivity().popBackStackOnce()
		else {
			curActivity().move2SendFragment(
				curNetworkType!!,
				curFingerPrint,
				shouldQRCleared = false
			)
		}
	}

	private val animFadeInBackBtn: Animation by lazy {
		AnimationUtils.loadAnimation(requireContext(), R.anim.btn_effect)
	}

	private fun curActivity() = requireActivity() as MainActivity

}
