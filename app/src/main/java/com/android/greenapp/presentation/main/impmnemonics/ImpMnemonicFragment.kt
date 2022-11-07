package com.android.greenapp.presentation.main.impmnemonics

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.*
import android.view.inputmethod.EditorInfo
import android.webkit.*
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import cash.z.ecc.android.bip39.Mnemonics
import com.android.greenapp.R
import com.android.greenapp.databinding.FragmentImpmnemonicBinding
import com.android.greenapp.domain.entity.Wallet
import com.android.greenapp.presentation.App
import com.android.greenapp.presentation.custom.AnimationManager
import com.android.greenapp.presentation.custom.CustomEdtText
import com.android.greenapp.presentation.custom.DialogManager
import com.android.greenapp.presentation.custom.convertListToStringWithSpace
import com.android.greenapp.presentation.di.factory.ViewModelFactory
import com.android.greenapp.presentation.main.MainActivity
import com.android.greenapp.presentation.tools.METHOD_CHANNEL_GENERATE_HASH
import com.android.greenapp.presentation.tools.getColorResource
import com.android.greenapp.presentation.tools.getDrawableResource
import com.android.greenapp.presentation.tools.getStringResource
import com.android.greenapp.presentation.viewBinding
import com.example.common.tools.VLog
import com.example.common.tools.getPrefixForAddressFromNetworkType
import dagger.android.support.DaggerDialogFragment
import dev.b3nedikt.restring.Restring
import io.flutter.plugin.common.MethodChannel
import kotlinx.android.synthetic.main.fragment_impmnemonic.*
import kotlinx.android.synthetic.main.fragment_impmnemonic.linearLayout12
import kotlinx.android.synthetic.main.fragment_verification_wallet.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.content.ClipboardManager
import android.content.Context
import android.text.*
import com.android.greenapp.domain.entity.Token


/**
 * Created by bekjan on 14.04.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class ImpMnemonicFragment : DaggerDialogFragment() {

	private val binding by viewBinding(FragmentImpmnemonicBinding::bind)
	private var jobAfter7Seconds: Job? = null
	private var jobToMakeDuplicatesWarningGone: Job? = null

	@Inject
	lateinit var viewModelFactory: ViewModelFactory
	private val impMnemonicViewModel: ImpMnemonicViewModel by viewModels { viewModelFactory }

	@Inject
	lateinit var effect: AnimationManager

	@Inject
	lateinit var dialogManager: DialogManager

	private val duplicateWordsWithEdts = hashMapOf<String, MutableSet<EditText>>()
	private val edtWithPrevValues = hashMapOf<EditText, String>()

	private val edtOf12 = mutableSetOf<EditText>()
	private val edtOf24 = mutableSetOf<EditText>()


	companion object {
		const val NETWORK_TYPE_KEY = "network_type_key"
	}

	private var curNetworkType: String = ""

	private var importJob: Job? = null
	var allowedChars = "abcdefghijklmnopqrstuvwxyz"

	private val filter = object : InputFilter {
		override fun filter(
			p0: CharSequence?,
			p1: Int,
			p2: Int,
			p3: Spanned?,
			p4: Int,
			p5: Int
		): CharSequence? {
			if (p0 == null)
				return p0
			val builder = StringBuilder()
			for (i in p1 until p2) {
				if (!allowedChars.contains(p0[i]))
					builder.append("")
				else
					builder.append(p0[i])
			}
			return builder
		}

	}


	private var defaultTokensOnMainScreen = mutableListOf<Token>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		arguments?.let {
			curNetworkType = it.getString(NETWORK_TYPE_KEY) ?: ""
		}
		VLog.d("OnCreate on Import Mnemonics")
	}

	override fun getTheme(): Int {
		return R.style.DialogTheme
	}


	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		VLog.d("OnCreate View on Import Mnemonics on $dialog")
		val view = inflater.inflate(R.layout.fragment_impmnemonic, container, false)
		curActivity().impMnemonicsFragmentView = view
		return view
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		super.onViewCreated(view, savedInstanceState)
		VLog.d("OnViewCreated on Import Mnemonics")
		binding.btn12Words.isChecked = true
		registerButtonClicks()
		addingAddTextChangeListenerForEveryEdt(binding.linearLayout12)
		addingAddTextChangeListenerForEveryEdt(binding.linearLayout24)
		addingNumToFront()
//		highlightingWordTermsOfUseSecondVersion()
		initDefaultTokenMainScreen()
	}


	private fun initDefaultTokenMainScreen() {
		lifecycleScope.launch {
			defaultTokensOnMainScreen.clear()
			defaultTokensOnMainScreen.addAll(impMnemonicViewModel.getTokenDefaultOnMainScreen())
		}
	}


	private fun restrictingUsedEnteringNonEnglishChars(linear: LinearLayout) {

		try {
			for (lin in linear.children) {
				lin as LinearLayout
				for (edt in lin.children) {
					edt as EditText
					edt.filters = arrayOf(filter)
				}
			}

		} catch (ex: java.lang.Exception) {
			VLog.d("Exception occurred in restring user entering non english letter " + ex.message)
		}
	}


	@SuppressLint("SetTextI18n")
	private fun addingNumToFront() {
		binding.apply {
			val cur12Txt = btn12Words.text.toString()
			val cur24Txt = btn24Words.text.toString()
			btn12Words.text = "12 ${cur12Txt.lowercase()}"
			btn24Words.text = "24 ${cur24Txt.lowercase()}"
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
			val clickableSpan = object : ClickableSpan() {
				override fun onClick(p0: View) {
					VLog.d("Terms been clicked on impmnemonicsfragment")
					curActivity().move2CoinsDetailsFragment(
						curNetworkType,
						forImportMnemonics = true
					)
				}
			}
			ss.setSpan(
				fcsGreen,
				startingIndex,
				text.length,
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
			)
			ss.setSpan(
				underlineSpan,
				startingIndex,
				text.length,
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
			)
			ss.setSpan(
				clickableSpan,
				startingIndex,
				text.length,
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
			)
			binding.apply {
				checkboxText.text = ss
				checkboxText.movementMethod = LinkMovementMethod.getInstance()
			}
		} catch (ex: Exception) {
			VLog.d("Exception occurred : ${ex.message}")
		}
	}

	private fun getArrayOf12WordsFromLinear12(): Array<String> {
		val array_12_words = Array(12) { "" }
		var index = 0
		for (i in 0 until 6) {
			val pair = binding.linearLayout12.getChildAt(i)
			pair as LinearLayout
			val edt1 = pair.getChildAt(0) as EditText
			if (edt1.text.toString().isNotEmpty()) {
				array_12_words[index] = edt1.text.toString()
			}
			index++

		}
		for (i in 0 until 6) {
			val pair = binding.linearLayout12.getChildAt(i)
			pair as LinearLayout
			val edt2 = pair.getChildAt(1) as EditText
			if (edt2.text.toString().isNotEmpty()) {
				array_12_words[index] = edt2.text.toString().trim()
			}
			index++
		}
		VLog.d("Copying 12 Words From Linear 12 :  ${array_12_words.toList()}")
		return array_12_words
	}

	private fun getArrayOf12WordsFromLinear24(): Array<String> {
		val array_12_words = Array(12) { "" }
		var index = 0
		for (i in 0 until 12) {
			val pair = binding.linearLayout24.getChildAt(i)
			pair as LinearLayout
			val edt1 = pair.getChildAt(0) as EditText
			if (edt1.text.toString().isNotEmpty()) {
				array_12_words[index] = edt1.text.toString().trim()
			}
			index++
		}
		VLog.d("Copying 12 Words  From Linear 24 :  ${array_12_words.toList()}")
		return array_12_words
	}

	private fun settingArrayOf12WordsToLinear12(list12Words: Array<String>) {
		var index = 0
		for (i in 0 until 6) {
			val pair = binding.linearLayout12.getChildAt(i)
			pair as LinearLayout
			val edt1 = pair.getChildAt(0) as EditText
			if (list12Words[index] != "") {
				edt1.setText(list12Words[index])
				turningIntoRedIfEmpty(binding.linearLayout12)
			}
			index++
		}
		for (i in 0 until 6) {
			val pair = binding.linearLayout12.getChildAt(i)
			pair as LinearLayout
			val edt2 = pair.getChildAt(1) as EditText
			if (list12Words[index] != "") {
				edt2.setText(list12Words[index])
				turningIntoRedIfEmpty(binding.linearLayout12)
			}
			index++
		}
	}

	private fun settingArrayOf12WordsToLinear24(list12Words: Array<String>) {
		kotlin.runCatching {
			var index = 0
			for (i in 0 until 12) {
				val pair = binding.linearLayout24.getChildAt(i)
				pair as LinearLayout
				val edt1 = pair.getChildAt(0) as EditText
				if (list12Words[index] != "") {
					edt1.setText(list12Words[index])
					turningIntoRedIfEmpty(binding.linearLayout24)
				}
				index++
			}
		}.onFailure {
			VLog.d("Bug with watcher in edtText : ${it.message}")
		}
	}


	private fun registerButtonClicks() {
		binding.checkboxAgree.setOnCheckedChangeListener { p0, p1 ->
			if (p1) {
				binding.btnContinue.isEnabled =
					if (btn12Words.isChecked) edtOf12.size == 12 else edtOf24.size == 24
			} else
				binding.btnContinue.isEnabled = false
		}
		binding.radioGroupWordCount.setOnCheckedChangeListener { p0, p1 ->
			if (p1 == R.id.btn12Words) {
				binding.scroll12Words.visibility = View.VISIBLE
				binding.scroll24Words.visibility = View.GONE
				adjustingAssureTxt(12)
				settingArrayOf12WordsToLinear12(getArrayOf12WordsFromLinear24())
			} else {
				binding.scroll12Words.visibility = View.GONE
				binding.scroll24Words.visibility = View.VISIBLE
				adjustingAssureTxt(12)
				settingArrayOf12WordsToLinear24(getArrayOf12WordsFromLinear12())
			}
			binding.txtWarning.visibility = View.GONE
			changeBtnContinueAvailable(edtOf12, edtOf24)
		}

		binding.btnContinue.setOnClickListener {
			VLog.d("BtnContinue Clicked on ImpMnemonics Fragment")
			it.startAnimation(effect.getBtnEffectAnimation())
			it.requestFocus()
			val layout =
				if (binding.btn12Words.isChecked) binding.linearLayout12 else binding.linearLayout24
			val hasEmpty = turningIntoRedIfEmpty(layout)
			if (hasEmpty) {
				binding.btnContinue.isEnabled = false
				changeToGreenBcg(layout)
				return@setOnClickListener
			}
			val res = checkingDuplicateMnemonicOrEmpty(layout)
			if (res == "same") {
				binding.txtWarning.apply {
					text =
						curActivity().getStringResource(R.string.import_mnemonics_same_words_error)
					visibility = View.VISIBLE
				}
				makeWarningGoneAfter7Seconds()
			} else if (res == "containwords") {
				adjustingAssureTxt(24)
			} else if (res == "empty")
				VLog.d("We have empty cells")
			else if (res == "valid") {
				VLog.d("Requesting mnemonics to the server on ImpMneFragment")
				importMnemonicsLocally()
			}
		}
		binding.backLayout.setOnClickListener {
			curActivity().popBackStackOnce()
			curActivity().showTwoBtmDialogsFromImportBackButton()
		}
		binding.btn12Words.isChecked = true
		handlingNextBtnPressOnLastEdtInFirstColumn()
		binding.apply {
			checkboxText.text =
				Html.fromHtml(curActivity().getStringResource(R.string.agreement_with_terms_of_use_chekbox))
			checkboxText.setMovementMethod(LinkMovementMethod.getInstance())
		}

	}

	private fun importMnemonicsLocally() {
		importJob?.cancel()
		importJob = lifecycleScope.launch {
			val mnemonics = getMnemonicsList()
			val walletExist = impMnemonicViewModel.checkIfMnemonicsExist(mnemonics, curNetworkType)
			if (walletExist.isPresent && walletExist.get()) {
				curActivity().apply {
					if(!this.isFinishing) {
						dialogManager.showFailureDialog(
							this,
							getStringResource(R.string.pop_up_failed_error_title),
							getStringResource(R.string.pop_up_failed_error_description_wallet_has_already_added),
							getStringResource(R.string.return_btn)
						) {

						}
						return@launch
					}
				}
			}
			dialogManager.showProgress(curActivity())
			VLog.d("Mnemonics om import : $mnemonics")
			curActivity().curMnemonicCode = Mnemonics.MnemonicCode(convertListToStringWithSpace(mnemonics))
			callingFlutterMethodToGenerateHash()
			listenToMethodCallFromFlutter()
		}
	}

	private fun listenToMethodCallFromFlutter() {
		val methodChannel = MethodChannel(
			(curActivity().application as App).flutterEngine.dartExecutor.binaryMessenger,
			METHOD_CHANNEL_GENERATE_HASH
		)
		methodChannel.setMethodCallHandler { call, callBack ->
			try {
				if (call.method == "getHash") {
					curActivity().curMnemonicCode.validate()
					val arguments = (call.arguments as HashMap<*, *>)
					val fingerPrint = arguments["fingerPrint"]!!.toString().toLong()
					val address = arguments["address"]!!.toString()
					val puzzle_hash = arguments["puzzle_hash"]!!.toString()
					val newWallet = Wallet(
						fingerPrint,
						"",
						puzzle_hash,
						address,
						getMnemonicsList(),
						curNetworkType,
						System.currentTimeMillis(),
						0.0,
						System.currentTimeMillis()
					)
					defaultTokensOnMainScreen.forEach {
						newWallet.hashListImported[it.hash] = arguments[it.hash]!!.toString()
					}
					impMnemonicViewModel.importNewWallet(newWallet) {
						dialogManager.hidePrevDialogs()
						showSuccessImportingDialog()
					}
				} else if (call.method == "exception") {
					VLog.d("Exception from methodCallHandler : ${call.arguments}")
					adjustingAssureTxt(24)
				}
			} catch (ex: Exception) {
				VLog.d("Exception in methodCallHandler in Android : ${ex.message}")
				dialogManager.hidePrevDialogs()
				adjustingAssureTxt(24)
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
		VLog.d("Calling flutter generate hash : $mnemonicString")
		methodChannel.invokeMethod("generateHash", map);
	}

	private fun showSuccessImportingDialog() {
		dialogManager.showSuccessDialog(
			curActivity(),
			curActivity().getStringResource(R.string.import_mnemonics_pop_up_sucsess_title),
			curActivity().getStringResource(R.string.import_mnemonics_pop_up_sucsess_description),
			curActivity().getStringResource(R.string.ready_btn),
		) {
			lifecycleScope.launch {
				curActivity().move2HomeFragment()
			}
		}
	}

	private fun getMnemonicsList(): List<String> {
		val leftSide = mutableListOf<String>()
		val rightSide = mutableListOf<String>()
		val checkedLayout = if (btn12Words.isChecked) linearLayout12 else linearLayout24
		var size = if (btn12Words.isChecked) 6 else 12
		for (i in 0 until size) {
			val pair = checkedLayout.getChildAt(i)
			pair as LinearLayout
			val edt1 = pair.getChildAt(0) as EditText
			val edt2 = pair.getChildAt(1) as EditText
			leftSide.add(edt1.text.toString().trim())
			rightSide.add(edt2.text.toString().trim())
		}
		VLog.d("LeftSide of Import Mnemonics : $leftSide")
		VLog.d("RightSide of Import Mnemonics : $rightSide")
		leftSide.addAll(rightSide)
		VLog.d("After adding right to left  : $leftSide")
		return leftSide.toList().map { it.trim() }.toList()
	}

	private fun changeToGreenBcg(layout: LinearLayout) {
		try {

		} catch (ex: Exception) {
			VLog.e("Exception Occured ")
		}
	}

	private fun adjustingAssureTxt(wordCount: Int) {
		if (wordCount == 12) {
			binding.txtAssuring.apply {
				background =
					ContextCompat.getDrawable(curActivity(), R.drawable.assuring_12_background)
				setTextColor(ContextCompat.getColor(curActivity(), R.color.assuring_txt_color))
				setText(curActivity().resources.getString(R.string.import_mnemonics_warning))
			}
		} else {
			binding.txtAssuring.apply {
				background =
					ContextCompat.getDrawable(curActivity(), R.drawable.assuring_24_background)
				setTextColor(ContextCompat.getColor(curActivity(), R.color.white))
				setText(curActivity().getStringResource(R.string.import_mnemonics_wrong_words_error))
				makeMultipleWordOrDigitWarningGone7()
			}
			binding.apply {
				if (btn12Words.isChecked)
					clearingEdts(linearLayout12)
				else
					clearingEdts(linearLayout24)
			}
			dialogManager.hidePrevDialogs()
		}
	}

	private fun makeMultipleWordOrDigitWarningGone7() {
		jobAfter7Seconds?.cancel()
		jobAfter7Seconds = lifecycleScope.launch {
			delay(5000)
			adjustingAssureTxt(12)
		}
	}

	private fun handlingNextBtnPressOnLastEdtInFirstColumn() {
		binding.edt1211.setOnEditorActionListener(object : TextView.OnEditorActionListener {
			override fun onEditorAction(p0: TextView?, p1: Int, p2: KeyEvent?): Boolean {
				if (p1 == EditorInfo.IME_ACTION_DONE) {
					binding.edt122.requestFocus()
					return true
				}
				return false
			}
		})
		binding.edt2423.setOnEditorActionListener(object : TextView.OnEditorActionListener {
			override fun onEditorAction(p0: TextView?, p1: Int, p2: KeyEvent?): Boolean {
				if (p1 == EditorInfo.IME_ACTION_DONE) {
					binding.ed242.requestFocus()
					return true
				}
				return false
			}
		})
	}

	private fun showImportingMnemonicPhraseProgress() {
		val progressDialog = dialogManager.showProgress(curActivity())
		temporaryShowSuccessImportedDialog(progressDialog)
	}

	private fun temporaryShowSuccessImportedDialog(dialog: Dialog) {
		lifecycleScope.launch {
			delay(2000)
			dialog.dismiss()
			dialogManager.showSuccessDialog(
				curActivity(),
				curActivity().getStringResource(R.string.import_mnemonics_pop_up_sucsess_title),
				curActivity().getStringResource(R.string.import_mnemonics_pop_up_sucsess_description),
				curActivity().getStringResource(R.string.ready_btn)
			) {
				showTempFailedImpMnemonicDialog()
			}
		}
	}

	private fun showTempFailedImpMnemonicDialog() {
		lifecycleScope.launch {
			delay(2000)
			dialogManager.showFailureDialog(
				curActivity(),
				curActivity().getStringResource(R.string.pop_up_failed_import_mnemonics_title),
				curActivity().getStringResource(R.string.pop_up_failed_error_description),
				curActivity().getStringResource(R.string.return_btn)
			) {

			}
		}
	}

	private fun clearingEdts(linearLayout: LinearLayout) {
		try {
			for (pair in linearLayout.children) {
				pair as LinearLayout
				for (edt in pair.children) {
					edt as EditText
					edt.background =
						curActivity().getDrawableResource(R.drawable.edt_mnemonic_green)
					edt.setTextColor(
						ResourcesCompat.getColor(
							resources,
							R.color.actual_color_mnemonic,
							null
						)
					)
					edt.setText("")
				}
			}
		} catch (ex: java.lang.Exception) {
			VLog.e("Exception occurred in clearingEdts ${ex.message}")
		}
	}

	private fun addingAddTextChangeListenerForEveryEdt(layout: LinearLayout) {
		var firstTime = true
		VLog.d("Layout Children : ${layout.childCount}")
		try {
			for (pairLayout in layout.children) {
				val everyPair = pairLayout as LinearLayout
				for (edt in everyPair.children) {
					val edtText = edt as CustomEdtText
					edtText.initEdtListener(object : CustomEdtText.EdtListener {
						override fun onPasteText() {
							fillingEdtWithCopiedWords()
						}
					})
					edtText.addTextChangedListener {
						if (it == null) return@addTextChangedListener
						if (firstTime) return@addTextChangedListener
						val trimed = it.toString().trim()
						VLog.d("Trimed TextView Changed : $trimed")
						if (trimed.isEmpty()
						) {
//                            edtText.background = ResourcesCompat.getDrawable(
//                                resources,
//                                R.drawable.edt_mneumonic_red,
//                                null
//                            )
							if (binding.btn24Words.isChecked)
								edtOf24.remove(edtText)
							else
								edtOf12.remove(edtText)
						} else {
							edtText.background = ResourcesCompat.getDrawable(
								resources,
								R.drawable.edt_mnemonic_green,
								null
							)
							edtText.setTextColor(
								ResourcesCompat.getColor(
									resources,
									R.color.actual_color_mnemonic,
									null
								)
							)

							if (binding.btn24Words.isChecked)
								edtOf24.add(edtText)
							else
								edtOf12.add(edtText)

							if (edtWithPrevValues.size != 0) {
								val prevValue =
									edtWithPrevValues[edtText] ?: return@addTextChangedListener
								VLog.d(
									"Prev Value : $prevValue  and CurValue : ${
										trimed.trim()
									}"
								)
								if (duplicateWordsWithEdts[prevValue] != null) {
//                                    VLog.d(
//                                        "Duplicate Word Edts Exist with given value : ${
//                                            duplicateWordsWithEdts[prevValue]!!
//                                        }"
//                                    )
									val setEdts = duplicateWordsWithEdts[prevValue]!!
									setEdts.remove(edt)
									if (setEdts.size == 1) {
										makeEdtBcgGreen(setEdts)
									}
								}
							}


						}
						changeBtnContinueAvailable(edtOf12, edtOf24)

					}

					edtText.addTextChangedListener(object : TextWatcher {
						override fun beforeTextChanged(
							p0: CharSequence?,
							p1: Int,
							p2: Int,
							p3: Int
						) {

						}

						override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
							if (p3 > 2 && p0 != null) {
//								VLog.d("On Text Changed  from clipboard  : $p0")
								val mnemonics = p0.split(" ").toList()
								if (edtText.text.toString() == mnemonics[0])
									return
								if (binding.btn12Words.isChecked) {
									fillEdtWithCopiedMnemonics12Words(
										mnemonics,
										binding.linearLayout12
									)
								} else {
									fillEdtWithCopiedMnemonics24Words(
										mnemonics,
										binding.linearLayout24
									)
								}
								edtText.setText(mnemonics[0])
							}
						}

						override fun afterTextChanged(p0: Editable?) {

						}

					})

				}
			}
		} catch (ex: Exception) {
			VLog.d("Exception : ${ex.message}")
		}
		firstTime = false
	}

	private fun fillingEdtWithCopiedWords() {
		val clipBoard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
		val copiedText = clipBoard.primaryClip?.getItemAt(0)?.text.toString()
		val mnemonics = copiedText.split(" ").map { it.trim() }.toList()
		VLog.d("Mnemonics text : $mnemonics")
		if (binding.btn12Words.isChecked) {
			fillEdtWithCopiedMnemonics12Words(mnemonics, binding.linearLayout12)
		} else {
			fillEdtWithCopiedMnemonics24Words(mnemonics, binding.linearLayout24)
		}
	}

	private fun fillEdtWithCopiedMnemonics24Words(
		mnemonics: List<String>,
		linearLayout: LinearLayout
	) {
		VLog.d("LinearLayoutCont for 24 Words : ${linearLayout.childCount}")
		var at = 0
		try {
			for (i in 0 until 12) {
				val pairLayout = linearLayout.getChildAt(i)
				val everyPair = pairLayout as LinearLayout
				val leftEdt = everyPair.getChildAt(0) as CustomEdtText
				if (leftEdt.text.toString().isEmpty() && at < mnemonics.size) {
					leftEdt.setText(mnemonics[at++])
				}
			}
			for (i in 0 until 12) {
				val pairLayout = linearLayout.getChildAt(i)
				val everyPair = pairLayout as LinearLayout
				val rightEdt = everyPair.getChildAt(1) as CustomEdtText
				if (rightEdt.text.toString().isEmpty() && at < mnemonics.size) {
					rightEdt.setText(mnemonics[at++])
				}
			}
		} catch (ex: java.lang.Exception) {
			VLog.d("Exception in filling edt with copied words : ${ex.message}")
		}
	}

	private fun fillEdtWithCopiedMnemonics12Words(
		mnemonics: List<String>,
		linearLayout: LinearLayout
	) {
		var at = 0
		try {
			for (pairLayout in linearLayout.children) {
				val everyPair = pairLayout as LinearLayout
				val leftEdt = everyPair.getChildAt(0) as CustomEdtText
				val rightEdt = everyPair.getChildAt(1) as CustomEdtText
				leftEdt.setText(mnemonics[at])
				rightEdt.setText(mnemonics[at + 6])
				at++
			}
		} catch (ex: java.lang.Exception) {
			VLog.d("Exception in filling edt with copied words : ${ex.message}")
		}
	}


	private fun changeBtnContinueAvailable(
		edtOf12: MutableSet<EditText>,
		edtOf24: MutableSet<EditText>
	) {
		VLog.d("EdtOf12 : ${edtOf12.size} and EdtOf24 : ${edtOf24.size}")
		binding.apply {
			val checked = checkboxAgree.isChecked
			if (checked) {
				if (btn12Words.isChecked) {
					btnContinue.isEnabled = edtOf12.size == 12
				} else {
					btnContinue.isEnabled = edtOf24.size == 24
				}
			}
		}
	}


	private fun containTwoWordsOrDigits(trim: CharSequence): Boolean {
		if (trim.contains(Regex("[0-9]"))) return true
		val split = trim.split(Regex("[\\s-]"))
		if (split.size >= 2) {
			return true
		}
		val len = trim.length
		return !trim.contains(Regex("[a-z]{$len}"))
	}

	private fun curActivity() = requireActivity() as MainActivity

	private fun makeEdtBcgGreen(edts: MutableSet<EditText>) {
		edts.forEach { edt ->
			edt.background = ResourcesCompat.getDrawable(
				resources,
				R.drawable.edt_mnemonic_green,
				null
			)
			edt.setTextColor(
				ResourcesCompat.getColor(
					resources,
					R.color.actual_color_mnemonic,
					null
				)
			)
		}
	}


	private fun setFocusChangeListenerForEveryEdts(layout: LinearLayout) {
		try {

			for (pair in layout.children) {
				val linear = pair as LinearLayout
				for (edt in linear.children) {
					edt as EditText
					edt.setOnFocusChangeListener { _, hasFocus ->
						if (hasFocus && duplicateWordsWithEdts.size != 0) {
							val curValue = edt.text.trim().toString()
							if (duplicateWordsWithEdts[curValue] != null) {
								VLog.d(
									"Duplicate Word Edts Exist with given value : ${
										duplicateWordsWithEdts[curValue]!!
									}"
								)
								val setEdts = duplicateWordsWithEdts[curValue]!!
								setEdts.remove(edt)
								if (setEdts.size == 1) {
									makeEdtBcgGreen(setEdts)
								}
							}
						}
					}
				}
			}

		} catch (ex: java.lang.Exception) {
			VLog.d("Exception : in settingFocusChangeListener + ${ex}")
		}
	}


	private fun checkingDuplicateMnemonicOrEmpty(layout: LinearLayout): String {
		val seenMap = hashMapOf<String, EditText>()
		duplicateWordsWithEdts.clear()
		var containMulWords = false
		var hasSeen = false
		try {
			for (pairLayout in layout.children) {
				val everyPair = pairLayout as LinearLayout
				for (edt in everyPair.children) {
					val edtText = (edt as EditText)
					val value = edtText.text.toString().trim()
					if (value.isEmpty()) return "empty"
					val curContainTwoWordsOrDigit = containTwoWordsOrDigits(value.trim())
					if (curContainTwoWordsOrDigit) {
						edtText.apply {
							setTextColor(
								ResourcesCompat.getColor(
									resources,
									R.color.red_mnemonic,
									null
								)
							)
							background = ResourcesCompat.getDrawable(
								resources,
								R.drawable.edt_mneumonic_red,
								null
							)
						}
					}
					containMulWords = containMulWords || curContainTwoWordsOrDigit
					if (duplicateWordsWithEdts[value] == null)
						duplicateWordsWithEdts[value] = mutableSetOf()
					duplicateWordsWithEdts[value]!!.add(edtText)
					edtWithPrevValues[edtText] = value
					if (seenMap[value] != null) {
						hasSeen = true

						edtText.apply {
							setTextColor(
								ResourcesCompat.getColor(
									resources,
									R.color.red_mnemonic,
									null
								)
							)
							background = ResourcesCompat.getDrawable(
								resources,
								R.drawable.edt_mneumonic_red,
								null
							)
						}
						val prevEdt = seenMap[value] ?: continue
						prevEdt.apply {
							setTextColor(
								ResourcesCompat.getColor(
									resources,
									R.color.red_mnemonic,
									null
								)
							)
							background = ResourcesCompat.getDrawable(
								resources,
								R.drawable.edt_mneumonic_red,
								null
							)
						}
					}
					if (seenMap[value] == null)
						seenMap[value] = edtText
				}
			}
		} catch (ex: Exception) {
			VLog.d("Exception : ${ex.message}")
		}
		if (containMulWords) return "containwords"
		VLog.d("Checking words Existence with respective edts : ${duplicateWordsWithEdts["о"]}")
		return if (hasSeen) "same" else "valid"
	}


	private fun turningIntoRedIfEmpty(layout: LinearLayout): Boolean {
		var isEmpty = false
		try {
			for (pairLayout in layout.children) {
				val everyPair = pairLayout as LinearLayout
				for (edt in everyPair.children) {
					val edtText = (edt as EditText)
					val value = edtText.text.toString()
					if (value.isEmpty()) {
						edtText.background = ResourcesCompat.getDrawable(
							resources,
							R.drawable.edt_mneumonic_red,
							null
						)
						isEmpty = true
					} else
						edtText.background = ResourcesCompat.getDrawable(
							resources,
							R.drawable.edt_mnemonic_green,
							null
						)
				}
			}
		} catch (ex: Exception) {
			VLog.d("Exception : ${ex.message}")
		}
		return isEmpty
	}

	private fun waiting7Seconds() {
		jobAfter7Seconds?.cancel()
		jobAfter7Seconds = lifecycleScope.launch {
			delay(6000)
			if (binding.btn12Words.isChecked)
				turningIntoRedIfEmpty(binding.linearLayout12)
			else
				turningIntoRedIfEmpty(binding.linearLayout24)
		}
	}

	private fun makeWarningGoneAfter7Seconds() {
		jobToMakeDuplicatesWarningGone?.cancel()
		jobToMakeDuplicatesWarningGone = lifecycleScope.launch {
			delay(5000)
			binding.txtWarning.visibility = View.GONE
		}
	}


	override fun onStart() {
		super.onStart()
		VLog.d("OnStart on import mnemonic fragment")
		val clearingEdts = areAllEdtsFilled()
		if (binding.checkboxAgree.isChecked && clearingEdts)
			binding.btnContinue.isEnabled = true
		initStatusBarColor()
	}

	private fun initStatusBarColor() {
		dialog?.apply {
			window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
			window?.statusBarColor = curActivity().getColorResource(R.color.cardView_background)
			window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		}
	}


	private fun areAllEdtsFilled(): Boolean {
		reinitializeEdts()
		VLog.d("Checked Mode : 12 on Start ->  ${btn12Words.isChecked} 24 -> ${btn24Words.isChecked} ")
		VLog.d("Size of egtOf12 on Start  : ${edtOf12.size} 24 -> ${edtOf24.size}")
		if (binding.btn12Words.isChecked)
			return edtOf12.size >= 12
		return edtOf24.size >= 24
	}

	private fun reinitializeEdts() {
		edtOf12.clear()
		edtOf24.clear()
		fillingEdtIfItHasTxt(binding.linearLayout12, edtOf12)
		fillingEdtIfItHasTxt(binding.linearLayout24, edtOf24)
	}

	private fun fillingEdtIfItHasTxt(layout: LinearLayout, edtOf12: MutableSet<EditText>) {
		try {
			for (pairLayout in layout.children) {
				val everyPair = pairLayout as LinearLayout
				for (edt in everyPair.children) {
					val edtText = edt as EditText
					if (edtText.text.toString().isNotEmpty()) {
						edtOf12.add(edtText)
					}
				}
			}
		} catch (ex: Exception) {
			VLog.d("Exception : ${ex.message}")
		}
	}

	override fun onResume() {
		super.onResume()
		VLog.d("OnResume on import mnemonic fragment")
	}

	override fun onPause() {
		super.onPause()
		VLog.d("OnPause on import mnemonic fragment")
	}

	override fun onStop() {
		super.onStop()
		VLog.d("OnStop on import mnemonic fragment")
	}

	override fun onDestroyView() {
		super.onDestroyView()
		jobAfter7Seconds?.cancel()
		jobToMakeDuplicatesWarningGone?.cancel()
		curActivity().impMnemonicsFragmentView = null
	}


	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		VLog.d("OnCreateDialog appeared on impMnemonicsFragment")
		dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		return super.onCreateDialog(savedInstanceState)
	}


}
