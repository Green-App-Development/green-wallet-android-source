package com.green.wallet.presentation.main.createnewwallet

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.common.tools.*
import com.green.wallet.R
import com.green.wallet.databinding.FragmentVerificationWalletBinding
import com.green.wallet.domain.domainmodel.Token
import com.green.wallet.domain.domainmodel.Wallet
import com.green.wallet.presentation.App
import com.green.wallet.presentation.custom.AnimationManager
import com.green.wallet.presentation.custom.DialogManager
import com.green.wallet.presentation.custom.convertListToStringWithSpace
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.main.MainActivity
import com.green.wallet.presentation.tools.*
import dagger.android.support.DaggerDialogFragment
import io.flutter.plugin.common.MethodChannel
import kotlinx.android.synthetic.main.fragment_verification_wallet.*
import kotlinx.android.synthetic.main.fragment_verification_wallet.view.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class VerificationFragment : DaggerDialogFragment() {

	private lateinit var binding: FragmentVerificationWalletBinding

	companion object {
		const val OPTIONS_WORDS_KEY = "options_words_key"
		const val NETWORK_TYPE_KEY = "network_type_key"
	}

	private lateinit var mnemonics: List<String>


	@Inject
	lateinit var viewModelFactory: ViewModelFactory
	private val newWalletViewModel: NewWalletViewModel by viewModels { viewModelFactory }

	@Inject
	lateinit var effect: AnimationManager

	@Inject
	lateinit var dialogManager: DialogManager

	private var jobVerified: Job? = null
	private var curNetworkType: String = ""

	private val leftSideWords = Array(6) { " " }

	private var optionsWords = MutableList(6) { " " }

	private var curOptionsWords = optionsWords.toMutableList()

	lateinit var curSelectedTxtPlaceHolder: TextView

	private var curSelectedTxtLocation = 0

	private var defaultTokensOnMainScreen = mutableListOf<Token>()


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		arguments?.let {
			mnemonics = it.getStringArrayList(OPTIONS_WORDS_KEY)!!
			VLog.d("Incoming Mnemonics for receiver : $mnemonics")
			settingLeftSideWordsAndOptions(mnemonics)
			if (it.getString(NETWORK_TYPE_KEY) != null) {
				curNetworkType = it.getString(NETWORK_TYPE_KEY)!!
			}
		}
	}

	override fun getTheme(): Int {
		return R.style.DialogTheme
	}

	private fun settingLeftSideWordsAndOptions(mnemonics: List<String>) {
		val options = Array(6) { "" }
		var left = 0
		var right = 6
		for (i in 0 until mnemonics.size step 2) {
			leftSideWords[left] = mnemonics[left++]
			options[right - 6] = mnemonics[right++]
		}
		optionsWords = arbitraryOrderOfWords(options.toList())
		curOptionsWords = arbitraryOrderOfWords(options.toList())
	}

	private fun arbitraryOrderOfWords(options: List<String>): MutableList<String> {
		val arbitraryOrder = MutableList(options.size) { "" }
		arbitraryOrder[0] = options[3]
		arbitraryOrder[1] = options[2]
		arbitraryOrder[2] = options[4]
		arbitraryOrder[3] = options[5]
		arbitraryOrder[4] = options[0]
		arbitraryOrder[5] = options[1]
		return arbitraryOrder
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentVerificationWalletBinding.inflate(inflater)
		getMainActivity().window.setFlags(
			WindowManager.LayoutParams.FLAG_SECURE,
			WindowManager.LayoutParams.FLAG_SECURE
		)
		return binding.root
	}


	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setUpInitialValues()
		registerClicks()
		updateOptionsValuesSecondVersion()
		initDefaultTokenMainScreen()
	}

	private fun initDefaultTokenMainScreen() {
		lifecycleScope.launch {
			defaultTokensOnMainScreen.clear()
			defaultTokensOnMainScreen.addAll(newWalletViewModel.getTokenDefaultOnMainScreen())
		}
	}


	@SuppressLint("SetTextI18n")
	private fun registerClicks() {

		//optionsTextViewClicks
		for (pairLinear in binding.linear6WordsOptions.children) {
			pairLinear as LinearLayout
			for (k in pairLinear.children) {
				val txtView = k as TextView
				txtView.setOnClickListener {
					if (this::curSelectedTxtPlaceHolder.isInitialized) {
						val curText = curSelectedTxtPlaceHolder.text
						var i = 0
						while (i < curText.length && Character.isDigit(curText[i]))
							i++
						val truncatedText = curText.substring(1 + i)
						if (truncatedText.isNotEmpty())
							return@setOnClickListener
						curSelectedTxtPlaceHolder.text =
							curSelectedTxtPlaceHolder.text.toString() + " " + txtView.text
						changingTextColorBasedMode(curSelectedTxtPlaceHolder)
						curSelectedTxtPlaceHolder.background =
							curActivity().getDrawableResource(R.drawable.focused_bcg_options)
						curOptionsWords.remove(txtView.text)
						updateOptionsValuesSecondVersion()
						setCLicksContinuously(
							++curSelectedTxtLocation,
							getRightSide6WordsPlaceHolders()
						)
					}
				}
			}
		}


		binding.btnContinue.setOnClickListener {
			it.startAnimation(effect.getBtnEffectAnimation())
			if (phrasesMatched()) {
				lifecycleScope.launch {
					dialogManager.showProgress(curActivity())
					listenToMethodCallFromFlutter()
					callingFlutterMethodToGenerateHash()
				}
			} else {
				warningWrongChoices()
			}
		}

		binding.backLayout.setOnClickListener {
			it.startAnimation(effect.getBtnEffectAnimation())
			curActivity().popBackStackOnce()
		}

//        binding.relWarningWrongChosen.setOnClickListener {
//            puttingToInitialState()
//        }

		setCLicksContinuously(curSelectedTxtLocation, getRightSide6WordsPlaceHolders())
	}

	private fun listenToMethodCallFromFlutter() {
		val methodChannel = MethodChannel(
			(curActivity().application as App).flutterEngine.dartExecutor.binaryMessenger,
			METHOD_CHANNEL_GENERATE_HASH
		)
		methodChannel.setMethodCallHandler { call, callBack ->
			if (call.method == "getHash") {
				val arguments = (call.arguments as HashMap<*, *>)
				VLog.d("Method from flutter side on android : $arguments")
				val fingerPrint = arguments["fingerPrint"]!!.toString().toLong()
				val address = arguments["address"]!!.toString()
				val main_hashes = arguments["main_puzzle_hashes"] as List<String>
				val newWallet = Wallet(
					fingerPrint,
					main_hashes,
					address,
					mnemonics,
					curNetworkType,
					home_id_added = System.currentTimeMillis(),
					0.0,
					savedTime = System.currentTimeMillis(),
					observerHash = 12,
					nonObserverHash = 5
				)
				defaultTokensOnMainScreen.forEach {
					newWallet.hashListImported[it.hash] =
						(arguments[it.hash] ?: emptyList<String>()) as List<String>
				}
				newWalletViewModel.createNewWallet(newWallet) {
					dialogManager.hidePrevDialogs()
					showCongratulateDialog()
				}
			}
		}

	}

	private fun callingFlutterMethodToGenerateHash() {
		val mnemonics = curActivity().curMnemonicCode.words.map { String(it) }.toList()
		val methodChannel = MethodChannel(
			(curActivity().application as App).flutterEngine.dartExecutor.binaryMessenger,
			METHOD_CHANNEL_GENERATE_HASH
		)
		val mnemonicString = convertListToStringWithSpace(mnemonics)
		val map = hashMapOf<String, Any>()
		map["mnemonic"] = mnemonicString
		map["prefix"] = getPrefixForAddressFromNetworkType(curNetworkType)
		map["tokens"] = convertListToStringWithSpace(defaultTokensOnMainScreen.map { it.hash })
//		map["observer"]=12
//		map["non_observer"]=5
		map["observer"] = 12
		map["non_observer"] = 5
		VLog.d("Invoked method generate hash : $mnemonicString")
		methodChannel.invokeMethod("generateHash", map)
	}

	private fun setCLicksContinuously(cur: Int, txts: Array<TextView?>) {
		var index = cur
		if (index >= txts.size) {
			index = txts.size
			curSelectedTxtLocation = index
		}
		VLog.d("Currently chosen : txtLocation  -> $index")
		if (index < txts.size) txts[index]?.apply {
			curSelectedTxtPlaceHolder = this
			curSelectedTxtPlaceHolder.background =
				curActivity().getDrawableResource(R.drawable.focused_bcg_options)
			this.setOnClickListener(null)
		}
		if (index >= 1) {
			txts[index - 1]?.setOnClickListener {
				val pair = getDigitAndStringSeparate(txts[index - 1]?.text.toString())
				txts[index - 1]?.text = "${pair.first}."
				curSelectedTxtLocation--
				setCLicksContinuously(curSelectedTxtLocation, getRightSide6WordsPlaceHolders())
				curOptionsWords.add(pair.second)
				updateOptionsValuesSecondVersion()
				if (index < txts.size) txts[index]?.background =
					curActivity().getDrawableResource(R.drawable.bcg_txt_mnemonic)
			}
		}
		if (index >= 2) {
			txts[index - 2]?.setOnClickListener(null)
		}
	}

	private fun getRightSide6WordsPlaceHolders(): Array<TextView?> {
		val placeHolder = Array<TextView?>(6) { null }
		var index = 0
		for (pair in binding.linearLayout12.children) {
			pair as LinearLayout
			val txt2 = pair.getChildAt(1) as TextView
			placeHolder[index++] = txt2
		}
		return placeHolder
	}

	private fun getDigitAndStringSeparate(txt: String): Pair<Int, String> {
		val split = txt.split(".")
		val integer = split[0].trim()
		var string = split[1].trim()
		val pair = Pair<Int, String>(Integer.valueOf(integer), string)
		return pair
	}

	private fun deprecatedPlaceHoldersForOptions() {
		for (pairLinear in binding.linearLayout12.children) {
			val linearLayWithEdt = pairLinear as LinearLayout
			linearLayWithEdt.getChildAt(1).setOnClickListener {
				VLog.d("CurSelected  Text : ${(it as TextView).text}")
				if (this::curSelectedTxtPlaceHolder.isInitialized) {
					var index = 0
					val text = curSelectedTxtPlaceHolder.text.toString()
					while (index < text.length && Character.isDigit(text[index])) {
						index++
					}
					val prevText = curSelectedTxtPlaceHolder.text.substring(1 + index)
					if (prevText.isEmpty()) {
						curSelectedTxtPlaceHolder.background =
							ContextCompat.getDrawable(curActivity(), R.drawable.bcg_txt_mnemonic)
					}
				}
				val curText = it.text
				var i = 0
				val number = StringBuilder()
				while (i < curText.length && Character.isDigit(curText[i])) {
					number.append(curText[i])
					i++
				}
				val truncatedText = curText.substring(1 + i)
				if (truncatedText.isNotEmpty()) {
					it.setText("$number.")
					curOptionsWords.add(truncatedText.trim())
					updateOptionsValuesSecondVersion()
					return@setOnClickListener
				}
				curSelectedTxtPlaceHolder = it
				VLog.d("EdtTxt Clicked")
				curSelectedTxtPlaceHolder.background =
					ContextCompat.getDrawable(curActivity(), R.drawable.focused_bcg_options)
			}
		}
	}


	private fun warningWrongChoices() {
		binding.relWarningWrongChosen.visibility = View.VISIBLE
		for (pair in binding.linearLayout12.children) {
			pair as LinearLayout
			val txt1 = pair.getChildAt(0) as TextView
			val txt2 = pair.getChildAt(1) as TextView
			txt2.text = txt2.text.toString()
			txt1.apply {
				background =
					curActivity().getDrawableResource(R.drawable.bcg_txt_mnemonic_wrong)
				text = separatingStringFromDigitSS(txt1.text.toString())
			}
			txt2.apply {
				background =
					curActivity().getDrawableResource(R.drawable.bcg_txt_mnemonic_wrong)
				text = separatingStringFromDigitSS(txt2.text.toString())
			}
			txt1.isEnabled = false
			txt2.isEnabled = false
		}
		lifecycleScope.launch {
			binding.btnContinue.isEnabled = false
			delay(4000)
			puttingToInitialState()
		}
	}

	private fun puttingToInitialState() {
		binding.apply {
			relWarningWrongChosen.visibility = View.GONE
			txtFillWithOptions.visibility = View.VISIBLE
		}
		setUpInitialValues()
		curOptionsWords.addAll(optionsWords)
		updateOptionsValuesSecondVersion()
		for (pair in binding.linearLayout12.children) {
			pair as LinearLayout
			val txt1 = pair.getChildAt(0) as TextView
			val txt2 = pair.getChildAt(1) as TextView
			txt1.apply {
				background =
					curActivity().getDrawableResource(R.drawable.bcg_txt_mnemonic)
			}
			txt2.apply {
				background =
					curActivity().getDrawableResource(R.drawable.bcg_txt_mnemonic)
			}
			txt1.isEnabled = true
			txt2.isEnabled = true
		}
		curSelectedTxtLocation = 0
		setCLicksContinuously(curSelectedTxtLocation, getRightSide6WordsPlaceHolders())
	}

	private fun separatingStringFromDigitSS(text: String): SpannableString {
		var index = 0
		while (index < text.length && Character.isDigit(text[index]))
			index++
		val ss = SpannableString(text)
		val fcsDigit = ForegroundColorSpan(
			ContextCompat.getColor(
				curActivity(),
				R.color.placed_option_word_number
			)
		)
		val fcsRest = ForegroundColorSpan(
			ContextCompat.getColor(
				curActivity(),
				R.color.red_mnemonic
			)
		)
		ss.setSpan(fcsDigit, 0, index, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
		ss.setSpan(fcsRest, index + 1, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
		return ss
	}

	private fun phrasesMatched(): Boolean {
		var left = 0
		var right = 6
		for (pair in binding.linearLayout12.children) {
			pair as LinearLayout
			val txt1 = (pair.getChildAt(0) as TextView).text.split(" ")[1].trim()
			val txt2 = (pair.getChildAt(1) as TextView).text.split(" ")[1].trim()
			VLog.d("Txt1 : $txt1  and Txt2 : $txt2")
			if (mnemonics[left++] != txt1 || mnemonics[right++] != txt2)
				return false
		}
		return true
	}

	private fun showCongratulateDialog() {
		lifecycleScope.launch {
			dialogManager.showSuccessDialog(
				curActivity(),
				curActivity().getStringResource(R.string.mnemonic_phrase_verification_pop_up_sucsess_title),
				curActivity().getStringResource(R.string.mnemonic_phrase_verification_pop_up_sucsess_description),
				curActivity().getStringResource(R.string.ready_btn),
			) {
				curActivity().move2HomeFragment()
			}
		}
	}


	private fun changingTextColorBasedMode(curSelectedEdtText: TextView) {
		val value = curSelectedEdtText.text.toString()
		var index = 0
		while (index < value.length && Character.isDigit(value[index]))
			index++
		val ss = SpannableString(value)
		val fcsDigit = ForegroundColorSpan(
			ContextCompat.getColor(
				curActivity(),
				R.color.placed_option_word_number
			)
		)
		val fcsRest = ForegroundColorSpan(
			ContextCompat.getColor(
				curActivity(),
				R.color.placed_option_word_phrase
			)
		)
		ss.setSpan(fcsDigit, 0, index, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
		ss.setSpan(fcsRest, index + 1, value.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
		curSelectedEdtText.text = ss
	}

	@SuppressLint("SetTextI18n")
	private fun setUpInitialValues() {
		var index = 1
		for (linear in binding.linearLayout12.children) {
			val pairLayout = linear as LinearLayout
			val firstTxtView = pairLayout.getChildAt(0) as TextView
			val secondTxtView = pairLayout.getChildAt(1) as TextView
			firstTxtView.text = "${index}. ${leftSideWords[index - 1]}"
			secondTxtView.text = "${index + 6}."
			index++
		}
	}

	private fun updateOptionsValuesSecondVersion() {
		VLog.d("Options Value Updated : $curOptionsWords")
		var isEmptyOptions = curOptionsWords.isEmpty()
		if (isEmptyOptions) {
			binding.txtFillWithOptions.visibility = View.GONE
			binding.btnContinue.isEnabled = true
		} else {
			binding.txtFillWithOptions.visibility = View.VISIBLE
			binding.btnContinue.isEnabled = false
		}
		var index = 0
		for (pair in binding.linear6WordsOptions.children) {
			pair as LinearLayout
			val txt1 = pair.getChildAt(0) as TextView
			val txt2 = pair.getChildAt(1) as TextView
			pair.visibility = View.VISIBLE
			txt1.visibility = View.VISIBLE
			txt2.visibility = View.VISIBLE
			if (index < curOptionsWords.size) {
				txt1.text = curOptionsWords[index]
				index++
			} else {
				pair.visibility = View.GONE
				continue
			}
			if (index < curOptionsWords.size) {
				txt2.text = curOptionsWords[index]
				index++
			} else
				txt2.visibility = View.INVISIBLE
		}

	}

	private fun curActivity() = requireActivity() as MainActivity

	override fun onDestroyView() {
		super.onDestroyView()
	}

	override fun onResume() {
		super.onResume()
		getMainActivity().window.setFlags(
			WindowManager.LayoutParams.FLAG_SECURE,
			WindowManager.LayoutParams.FLAG_SECURE
		)
	}

	override fun onPause() {
		super.onPause()
		getMainActivity().window.clearFlags(
			WindowManager.LayoutParams.FLAG_SECURE
		)
	}

	override fun onStart() {
		super.onStart()
		initStatusBarColor()
	}

	private fun initStatusBarColor() {
		dialog?.apply {
			window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
			window?.statusBarColor = curActivity().getColorResource(R.color.save_mnemonic_bcg)
		}
	}


}
