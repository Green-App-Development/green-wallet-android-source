package com.android.greenapp.presentation.main.createnewwallet

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import cash.z.ecc.android.bip39.Mnemonics
import com.android.greenapp.R
import com.android.greenapp.databinding.FragmentCoinsInfBinding
import com.android.greenapp.presentation.custom.AnimationManager
import com.android.greenapp.presentation.custom.DialogManager
import com.android.greenapp.presentation.custom.getShortNetworkType
import com.android.greenapp.presentation.di.factory.ViewModelFactory
import com.android.greenapp.presentation.main.MainActivity
import com.android.greenapp.presentation.viewBinding
import com.example.common.tools.VLog
import dagger.android.support.DaggerFragment
import dev.b3nedikt.restring.Restring
import kotlinx.android.synthetic.main.fragment_coins_inf.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class CoinsDetailsFragment : DaggerFragment() {

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

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		return inflater.inflate(R.layout.fragment_coins_inf, container, false)
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
		highlightingWordTermsOfUseSecondVersion()
		initForImportMnemonicOrNot()
		initCoinViewDetails()
	}

	private fun initCoinViewDetails() {
		lifecycleScope.launch {
			val coinDetail = viewModel.getCoinDetails(getShortNetworkType(curNetworkType))

			txtCoinDescription.text = coinDetail.description
			txtCoinName.setText(coinDetail.name)

			val characteristics = coinDetail.characteristics
			edtCharacteristics.setText(
				addDotForEachNewLineOfCharacteristics(
					characteristics
				)
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
			generateMnemonicsLocally()
		}

		binding.backLayout.setOnClickListener {
			curActivity().popBackStackOnce()
		}

	}

	private fun generateMnemonicsLocally() {
		curActivity().curMnemonicCode = recursiveMnemonicGenerator()
		val generatedList = curActivity().curMnemonicCode.words.map { String(it) }.toList()
		VLog.d("CurNetworkType on Network Details Fragment : $curNetworkType")
		curActivity().move2SaveMnemonicFragment(generatedList, curNetworkType)
	}

	private fun recursiveMnemonicGenerator(): Mnemonics.MnemonicCode {
		val mnemonicsCode = Mnemonics.MnemonicCode(Mnemonics.WordCount.COUNT_12)
		val mnemonicsSet = mnemonicsCode.toList().toSet()
		if (mnemonicsSet.size < 12)
			return recursiveMnemonicGenerator()
		return mnemonicsCode
	}

	private fun generate12WordMnemonics(): MutableList<String> {
		val mnemonicCode =
			Mnemonics.MnemonicCode(Mnemonics.WordCount.COUNT_12).words.map { String(it) }.toSet()
		if (mnemonicCode.size < 12)
			return generate12WordMnemonics()
		return mnemonicCode.toMutableList()
	}

	private fun curActivity() = requireActivity() as MainActivity

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
		VLog.d("OnDestroy  on CoindsDetailsFragment")
	}


}
