package com.green.wallet.presentation.onboard.terms

import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.method.ScrollingMovementMethod
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.green.wallet.R
import com.green.wallet.databinding.FragmentTermsofuseBinding
import com.green.wallet.presentation.custom.DialogManager
import com.green.wallet.presentation.custom.manageExceptionDialogsForRest
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.onboard.OnBoardActivity
import com.green.wallet.presentation.onboard.OnBoardViewModel
import com.green.wallet.presentation.viewBinding
import com.green.wallet.presentation.tools.Resource
import com.green.wallet.presentation.tools.getStringResource
import com.green.wallet.presentation.tools.VLog
import dagger.android.support.DaggerFragment
import dev.b3nedikt.restring.Restring
import kotlinx.coroutines.launch
import javax.inject.Inject

class TermsFragment : DaggerFragment() {

	private val binding by viewBinding(FragmentTermsofuseBinding::bind)
	private lateinit var curLang: String

	@Inject
	lateinit var viewModelFactory: ViewModelFactory
	private val viewModel: OnBoardViewModel by viewModels { viewModelFactory }

	@Inject
	lateinit var dialogManager: DialogManager

	companion object {
		const val LANG_KEY = "language_key"
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		arguments?.let {
			curLang = it.getString(LANG_KEY, "")
		}
		VLog.d("OnCreated on terms fragment after language List Fragment got called")
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		return inflater.inflate(R.layout.fragment_termsofuse, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		registerButtons()
		retriveAgreementText()
	}

	private fun retriveAgreementText() {
		lifecycleScope.launch {
			val res = viewModel.getGreetingAgreementText()
			when (res.state) {
				Resource.State.SUCCESS -> {
					val text = res.data
					binding.txtTerms.text = text
				}
				Resource.State.LOADING -> {

				}
				Resource.State.ERROR -> {
					VLog.d("Exception type : ${res.error}")
					manageExceptionDialogsForRest(curActivity(), dialogManager, res.error)
				}
			}
		}
	}

	private fun registerButtons() {

		binding.apply {
			backLayout.setOnClickListener {
				VLog.d("Backstack is cleared on termsFragment, now")
				curActivity().popBackStack()
			}

			checkboxAgree.setOnCheckedChangeListener { p0, p1 ->
				btnContinue.isEnabled = p1
			}


			btnContinue.setOnClickListener {
				curActivity().move2SetPasswordFragment()
			}
			txtTerms.movementMethod = ScrollingMovementMethod()
		}

		binding.apply {
			checkboxText.text =
				Html.fromHtml(curActivity().getStringResource(R.string.agreement_with_terms_of_use_chekbox))
			checkboxText.setMovementMethod(LinkMovementMethod.getInstance())
		}
	}

	private fun highlitingWordTermsOfUse() {
		val startingIndex = if (curLang == "en") 9 else 15
		val text = binding.checkboxAgree.text
		val ss = SpannableString(text)
		val fcsGreen = ForegroundColorSpan(resources.getColor(R.color.green))
		val underlineSpan = UnderlineSpan()
		ss.setSpan(fcsGreen, startingIndex, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
		ss.setSpan(underlineSpan, startingIndex, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
		binding.checkboxAgree.text = ss
	}

	private fun highlightingWordTermsOfUseSecondVersion() {
		try {
			val agreementText = binding.checkboxText.text.toString()
			val curText = agreementText.split(' ')
			VLog.d("Locale : ${Restring.locale}")
			var length = 0
			var countWords = if (Restring.locale.toString() == "ru") 2 else 3
			for (i in curText.size - 1 downTo curText.size - countWords) {
				length += curText[i].length
			}
			length += countWords - 1
			val startingIndex = agreementText.length - length
			VLog.d("Starting Index : $startingIndex")
			val text = agreementText
			val ss = SpannableString(text)
			val fcsGreen = ForegroundColorSpan(resources.getColor(R.color.green))
			val underlineSpan = UnderlineSpan()
			ss.setSpan(fcsGreen, startingIndex, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
			ss.setSpan(underlineSpan, startingIndex, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
			binding.checkboxText.text = ss
		} catch (ex: Exception) {
			VLog.d("Exception occurred : ${ex.message}")
		}
	}

	private fun curActivity() = requireActivity() as OnBoardActivity

	private val fadeInBackBtn: Animation by lazy {
		AnimationUtils.loadAnimation(requireContext(), R.anim.btn_effect)
	}


}
