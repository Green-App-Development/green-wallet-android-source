package com.green.wallet.presentation.intro.authenticate

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.biometrics.BiometricManager.Authenticators.*
import android.hardware.fingerprint.FingerprintManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt.*
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.green.wallet.presentation.tools.VLog
import com.green.wallet.R
import com.green.wallet.databinding.FragmentEnterPasswordIntroBinding
import com.green.wallet.presentation.App
import com.green.wallet.presentation.custom.AnimationManager
import com.green.wallet.presentation.custom.DialogManager
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.intro.IntroActViewModel
import com.green.wallet.presentation.intro.IntroActivity
import com.green.wallet.presentation.tools.getStringResource
import dagger.android.support.DaggerFragment
import dev.b3nedikt.reword.Reword
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executor
import javax.inject.Inject

class AuthFragmentIntro : DaggerFragment() {

	private lateinit var binding: FragmentEnterPasswordIntroBinding

	@Inject
	lateinit var factory: ViewModelFactory
	private val introActViewModel: IntroActViewModel by viewModels { factory }

	@Inject
	lateinit var effect: AnimationManager

	@Inject
	lateinit var dialog: DialogManager


	private var index = 0
	private val passCode = IntArray(6)
	private var jobAfter3sRedWarningGone: Job? = null
	private var clearCashJob: Job? = null

	private lateinit var biometricPrompt: androidx.biometric.BiometricPrompt
	private lateinit var executor: Executor
	private lateinit var biometricPromptInfo: PromptInfo


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentEnterPasswordIntroBinding.inflate(inflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		registerBtnClicks()
		fixTxtViewLateValue()
//		//temp
//		lifecycleScope.launch {
//			delay(200)
//			determineDestination()
//		}
	}

	private fun fixTxtViewLateValue() {
		lifecycleScope.launch {
			delay(500)
			Reword.reword(binding.root)
		}
	}

	@SuppressLint("WrongConstant")
	private fun askingFaceIdPermission() {
		VLog.d("Face Id Permission : clicked")

		executor = ContextCompat.getMainExecutor(curActivity())
		biometricPrompt = androidx.biometric.BiometricPrompt(
			curActivity(),
			executor,
			object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {

				override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
					super.onAuthenticationError(errorCode, errString)
					VLog.d("Error in authentication for face id : $errString")
				}

				override fun onAuthenticationSucceeded(result: AuthenticationResult) {
					super.onAuthenticationSucceeded(result)
					curActivity().move2TimeFragment()
				}

				override fun onAuthenticationFailed() {
					super.onAuthenticationFailed()
					VLog.d("Authentication  Failed")
				}
			})

		biometricPromptInfo = PromptInfo.Builder()
			.setTitle(curActivity().getStringResource(R.string.bio_authetication))
			.setSubtitle(curActivity().getStringResource(R.string.bio_login))
			.setNegativeButtonText(curActivity().getStringResource(R.string.bio_cancel_btn))
			.setConfirmationRequired(false)
			.build()

		val bm = BiometricManager.from(curActivity())

		if (bm.canAuthenticate() != android.hardware.biometrics.BiometricManager.BIOMETRIC_SUCCESS) {
			VLog.d("Biometric manager is not supported")
			return
		}

		biometricPrompt.authenticate(biometricPromptInfo)
	}


	override fun onStart() {
		super.onStart()
		index = 0
		binding.circles.usedCircleCount = index
		VLog.d("On Start on auth fragment intro")
	}

	private fun registerBtnClicks() {

		for (linear in binding.btnsGridLayout.children) {
			linear as LinearLayoutCompat
			for (btnNum in linear.children)
				btnNum.setOnClickListener {
					if (index == 6)
						return@setOnClickListener
					if (btnNum is Button) {
						val n = Integer.valueOf(btnNum.text.toString())
						passCode[index++] = n
						binding.circles.usedCircleCount = index
					} else if (btnNum is RelativeLayout) {
						if (btnNum.tag == "rel_img_back_space") {
							btnNum.startAnimation(effect.getBtnEffectAnimation())
							index--
							if (index < 0)
								index = 0
							binding.circles.usedCircleCount = index
						} else if (btnNum.tag == "rel_face_id") {
							kotlin.runCatching {
								askingFaceIdPermission()
							}
						}
					}
					if (index == 6) {
						verifiedPasscode(passCode)
					}
				}
		}

		binding.txtClearCash.setOnClickListener {

			val btnConfirm = {
				introActViewModel.clearRoomDataStore() {
					curActivity().move2OnboardingActivity(true)
				}
			}

			val btnCancel = {

			}

			dialog.showConfirmationDialog(
				curActivity(),
				curActivity().getStringResource(R.string.reset_app_pop_up_title),
				curActivity().getStringResource(R.string.reset_app_pop_up_description),
				btnConfirm,
				btnCancel
			)

		}

	}

	private fun isFaceIDAvailable(): Boolean {
		val fingerprintManager: FingerprintManager =
			context!!.getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager
		VLog.d("FingerPrintManager hardware detected : ${fingerprintManager.isHardwareDetected}")
		if (fingerprintManager.isHardwareDetected) {
			return false
		}
		return true
	}


	private fun verifiedPasscode(passCode: IntArray) {
		lifecycleScope.launch {
			val savedPassCode = introActViewModel.getSavedPasscode()
			if (passCode.toList().toString() == savedPassCode) {
				determineDestination()
			} else {
				showMessagePasscodesDontMatch()
			}
		}
	}

	private fun determineDestination() {
		if (!(curActivity().application as App).applicationIsAlive) {
			curActivity().move2TimeFragment()
		} else {
			curActivity().move2MainActivity()
		}
	}

	private fun showMessagePasscodesDontMatch() {
		index = 0
		binding.circles.usedCircleCount = index
		binding.circles.shouldBeRedAll = true
		binding.txtWrongPassCodeEntered.visibility = View.VISIBLE
		jobAfter3sRedWarningGone?.cancel()
		jobAfter3sRedWarningGone = lifecycleScope.launch {
			delay(2000)
			index = 0
			binding.circles.usedCircleCount = index
			binding.circles.shouldBeRedAll = false
			binding.txtWrongPassCodeEntered.visibility = View.GONE
		}
	}

	private fun curActivity() =
		requireActivity() as IntroActivity


	override fun onResume() {
		super.onResume()
		VLog.d("On Resume on auth fragment intro")
	}

	override fun onPause() {
		super.onPause()
	}

	override fun onStop() {
		super.onStop()

	}


}
