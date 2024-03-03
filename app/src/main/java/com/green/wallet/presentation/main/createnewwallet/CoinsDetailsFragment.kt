package com.green.wallet.presentation.main.createnewwallet

import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.green.wallet.presentation.tools.VLog
import com.green.wallet.R
import com.green.wallet.databinding.FragmentCoinsInfBinding
import com.green.wallet.presentation.custom.AnimationManager
import com.green.wallet.presentation.custom.DialogManager
import com.green.wallet.presentation.custom.getShortNetworkType
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.main.MainActivity
import com.green.wallet.presentation.tools.getColorResource
import com.green.wallet.presentation.tools.getStringResource
import com.green.wallet.presentation.viewBinding
import dagger.android.support.DaggerDialogFragment
import dev.b3nedikt.restring.Restring
import kotlinx.android.synthetic.main.fragment_coins_inf.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import javax.inject.Inject

class CoinsDetailsFragment : DaggerDialogFragment() {

	private val binding by viewBinding(FragmentCoinsInfBinding::bind)

	@Inject
	lateinit var viewModelFactory: ViewModelFactory
	private val viewModel: NewWalletViewModel by viewModels { viewModelFactory }

	companion object {
		const val NETWORK_KEY: String = "network_key"
		const val FOR_IMPORT_MNEMONICS_KEY = "for_import_mnemonics_key"
	}

	private var curNetworkType: String = "Chia Network"
	private var for_import_mnemonics = false

	@Inject
	lateinit var effect: AnimationManager

	@Inject
	lateinit var dialogManager: DialogManager

	private val handler = CoroutineExceptionHandler { _, ex ->
		VLog.d("Exception handler in coind details : ${ex.message}")
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		val view = inflater.inflate(R.layout.fragment_coins_inf, container, false)
		return view
	}

	override fun getTheme(): Int {
		return R.style.DialogTheme
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		arguments?.let {
			curNetworkType = it.getString(NETWORK_KEY)!!
			for_import_mnemonics = it.getBoolean(FOR_IMPORT_MNEMONICS_KEY, false)
		}
	}


	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		registerButtonClicks()
		determineCoinImg()
//		highlightingWordTermsOfUseSecondVersion()
		initForImportMnemonicOrNot()
		initCoinViewDetails()
	}

	private fun initCoinViewDetails() {
		lifecycleScope.launch(handler) {
			val coinDetail = viewModel.getCoinDetails(getShortNetworkType(curNetworkType))

			txtCoinDescription.text = coinDetail.description
			txtCoinName.text = coinDetail.name

			val characteristics = coinDetail.characteristics
			edtCharacteristics.text = addDotForEachNewLineOfCharacteristics(
				characteristics
			)
		}
	}

	private fun addDotForEachNewLineOfCharacteristics(text: String): String {

		val lines = text.split("\n")
		var addedDotText = ""

		for (i in lines.indices) {
			val line = lines[i]
			val addedDot = "• $line ${if (i != lines.size - 1) "\r\n" else ""}"
			addedDotText += addedDot
		}
		return addedDotText
	}

	private fun initForImportMnemonicOrNot() {
		if (for_import_mnemonics) {
			linear_terms.visibility = View.INVISIBLE
			btnCreate.visibility = View.INVISIBLE
		}
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

	private fun determineCoinImg() {
		if (curNetworkType.lowercase().contains("chia")) {
			binding.imgCoin.setImageResource(R.drawable.ic_chia)
		} else {
			binding.imgCoin.setImageResource(R.drawable.ic_chives)
		}
	}

	private fun registerButtonClicks() {

		binding.checkboxAgree.setOnCheckedChangeListener { p0, p1 ->
			binding.btnCreate.isEnabled = p1

		}

		binding.btnCreate.setOnClickListener {
			curActivity().move2ProgressCreatingWalletFragment(networkType = curNetworkType)
		}

		binding.backLayout.setOnClickListener {
			curActivity().popBackStackOnce()
		}
		binding.apply {
			checkboxText.text =
				Html.fromHtml(curActivity().getStringResource(R.string.agreement_with_terms_of_use_chekbox))
			checkboxText.setMovementMethod(LinkMovementMethod.getInstance())
		}
	}

	private fun curActivity() = requireActivity() as MainActivity

	override fun onStart() {
		super.onStart()
		initStatusBarColor()
	}

	override fun onPause() {
		super.onPause()
		VLog.d("OnPause on Coins Details Fragment")
	}

	override fun onStop() {
		super.onStop()
		VLog.d("OnStop on Coins Details Fragment")
	}

	override fun onDestroyView() {
		super.onDestroyView()
		VLog.d("OnDestroy View on CoinsDetailsFragment")
	}

	override fun onDestroy() {
		super.onDestroy()
		VLog.d("OnDestroy  on CoinsDetailsFragment")
	}


	private fun initStatusBarColor() {
		dialog?.apply {
			window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
			window?.statusBarColor = curActivity().getColorResource(R.color.cardView_background)
		}
	}


}
