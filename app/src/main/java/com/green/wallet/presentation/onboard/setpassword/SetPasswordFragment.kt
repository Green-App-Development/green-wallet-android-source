package com.green.wallet.presentation.onboard.setpassword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import androidx.core.view.children
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.android.greenapp.R
import com.android.greenapp.databinding.FragmentSetPasswordBinding
import com.green.wallet.presentation.custom.AnimationManager
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.onboard.OnBoardActivity
import com.green.wallet.presentation.onboard.OnBoardViewModel
import com.green.wallet.presentation.viewBinding
import com.example.common.tools.VLog
import com.green.wallet.presentation.tools.getStringResource
import dagger.android.support.DaggerFragment
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


class SetPasswordFragment : DaggerFragment() {

	private val binding: FragmentSetPasswordBinding by viewBinding(FragmentSetPasswordBinding::bind)
	private var atPasswordIndex = 0
	private val firstPassword = IntArray(6)
	private val secondPassword = IntArray(6)
	private var curPasswordFirst = true
	private var jobAfter3SecondShowRedColor: Job? = null

	@Inject
	lateinit var viewModelFactory: ViewModelFactory
	private val greetingViewModel: OnBoardViewModel by viewModels { viewModelFactory }


	@Inject
	lateinit var effect: AnimationManager


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		val binding = FragmentSetPasswordBinding.inflate(layoutInflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		registerButtonClicks()
	}


	private fun registerButtonClicks() {
		binding.backLayout.setOnClickListener {
			it.startAnimation(effect.getBtnEffectAnimation())
			curActivity().popBackStack()
		}
		for (k in binding.btnsGridLayout.children) {
			k.setOnClickListener {
				savingFirstTimePasscodeLogic(it)
			}
		}
	}

	private fun savingFirstTimePasscodeLogic(it: View) {
		VLog.d("AtPasswordIndex : ${atPasswordIndex}")
		binding.circles.shouldBeRedAll = false
		binding.txtPassword6DigitsWarning.visibility = View.GONE
		if (it is RelativeLayout) {
			it.startAnimation(effect.getBtnEffectAnimation())
			if (atPasswordIndex - 1 >= 0) {
				atPasswordIndex--
				binding.circles.usedCircleCount = atPasswordIndex
			}
		} else if (it is Button) {
			if (curPasswordFirst) {
				firstPassword[atPasswordIndex++] = it.text.toString()[0] - '0'
				if (atPasswordIndex == 6) {
					binding.circles.usedCircleCount = 0
					atPasswordIndex = 0
					curPasswordFirst = false
					changeTextHeaderPassword()
					jobAfter3SecondShowRedColor?.cancel()
				} else {
					binding.circles.usedCircleCount = atPasswordIndex
				}
			} else {
				if (atPasswordIndex + 1 < 7) {
					secondPassword[atPasswordIndex++] = it.text.toString()[0] - '0'
					binding.circles.usedCircleCount = atPasswordIndex
					if (atPasswordIndex == 6) {
						if (firstPassword.toList() == secondPassword.toList()) {
							greetingViewModel.savePasscode(
								firstPassword.toList().toString()
							)
							greetingViewModel.saveUserUnBoarded(boarded = true)
							greetingViewModel.saveLastVisitedValue(System.currentTimeMillis())
							curActivity().move2TimeFragment()
						} else {
							showMessagePasscodesDontMatch()
							atPasswordIndex = 0
							binding.circles.usedCircleCount = atPasswordIndex
						}
					}
				}
			}
		}
	}

	private fun showMessagePasscodesDontMatch() {
		binding.circles.shouldBeRedAll = true
		binding.txtPassword6DigitsWarning.apply {
			text = curActivity().getStringResource(R.string.creating_a_password_error_difference)
			visibility = View.VISIBLE
		}
		jobAfter3SecondShowRedColor?.cancel()
		jobAfter3SecondShowRedColor = lifecycleScope.launch {
			delay(2000)
			binding.circles.shouldBeRedAll = false
			binding.txtPassword6DigitsWarning.visibility = View.GONE
		}
	}

	private fun triggerJobAfter3Seconds() {
		jobAfter3SecondShowRedColor?.cancel()
		jobAfter3SecondShowRedColor = lifecycleScope.launch {
			delay(3000)
			binding.circles.shouldBeRedAll = true
			binding.txtPassword6DigitsWarning.text =
				curActivity().getStringResource(R.string.creating_a_password_error_amount_of_characters)
			binding.txtPassword6DigitsWarning.visibility = View.VISIBLE
		}
	}

	private fun changeTextHeaderPassword() {
		binding.txtCreatCodePassword.text =
			curActivity().getStringResource(R.string.repeat_passcode_title)
		binding.txtPassCodeDetail.text =
			curActivity().getStringResource(R.string.repeat_passcode_description)
	}

	private fun curActivity() = requireActivity() as OnBoardActivity


}
