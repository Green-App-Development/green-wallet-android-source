package com.android.greenapp.presentation.main.send

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.webkit.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.android.greenapp.R
import com.android.greenapp.data.network.dto.greenapp.network.NetworkItem
import com.android.greenapp.data.preference.PrefsManager
import com.android.greenapp.databinding.FragmentSendBinding
import com.android.greenapp.domain.entity.Address
import com.android.greenapp.domain.entity.TokenWallet
import com.android.greenapp.domain.entity.WalletWithTokens
import com.android.greenapp.presentation.App
import com.android.greenapp.presentation.custom.*
import com.android.greenapp.presentation.di.factory.ViewModelFactory
import com.android.greenapp.presentation.main.MainActivity
import com.android.greenapp.presentation.tools.*
import com.android.greenapp.presentation.viewBinding
import com.example.common.tools.*
import com.google.gson.Gson
import dagger.android.support.DaggerFragment
import io.flutter.plugin.common.MethodChannel
import kotlinx.android.synthetic.main.dialog_confirm_transactions.*
import kotlinx.android.synthetic.main.dialog_confirm_transactions.txtAddress
import kotlinx.android.synthetic.main.dialog_confirm_transactions.txtBlockChain
import kotlinx.android.synthetic.main.dialog_notification_detail.*
import kotlinx.android.synthetic.main.fragment_impmnemonic.*
import kotlinx.android.synthetic.main.fragment_listing.*
import kotlinx.android.synthetic.main.fragment_receive.*
import kotlinx.android.synthetic.main.fragment_send.*
import kotlinx.android.synthetic.main.fragment_send.chosenNetworkRel
import kotlinx.android.synthetic.main.fragment_send.imgIconSpinner
import kotlinx.android.synthetic.main.fragment_send.network_spinner
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import javax.inject.Inject


/**
 * Created by bekjan on 21.04.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class SendFragment : DaggerFragment() {

	private val binding by viewBinding(FragmentSendBinding::bind)
	private lateinit var networkAdapter: NetworkAdapter
	private lateinit var tokenAdapter: NetworkAdapter
	private lateinit var walletAdapter: WalletListAdapter

	@Inject
	lateinit var dialogManager: DialogManager

	private var curNetworkType: String = ""
	private var curFingerPrint: Long? = null
	private var shouldQRBeEmpty: Boolean = false

	companion object {
		const val NETWORK_TYPE_KEY = "coin_type"
		const val FINGERPRINT_KEY = "finger_print"
		const val SHOULD_BE_EMPTY = "should_be_empty"
	}


	@Inject
	lateinit var viewModelFactory: ViewModelFactory
	private val viewModel: SendFragmentViewModel by viewModels { viewModelFactory }

	@Inject
	lateinit var anim: AnimationManager

	@Inject
	lateinit var prefsManager: PrefsManager

	@Inject
	lateinit var gson: Gson

	private var updateJob: Job? = null
	private var sendTransJob: Job? = null
	private var addressAlreadyExist: Job? = null
	private var commissionJob: Job? = null
	private var genSpendBundleJob: Job? = null


	private var curSendAddressWallet: String = ""
	private var availableAmount: Double = 0.0
	private var chosenTokenCode: String = ""
	private var walletAdapterPosition = 0

	private val handler = CoroutineExceptionHandler { _, ex ->
		VLog.d("Exception caught on send fragment : ${ex.message} ")
	}

	private var chosenTokenWalletList = listOf<TokenWallet>()
	private val twoEdtsFilled = mutableSetOf<Int>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		arguments?.let {
			curNetworkType = arguments?.getString(NETWORK_TYPE_KEY) ?: "Chia Network"
			if (it[FINGERPRINT_KEY] != null) {
				curFingerPrint = it[FINGERPRINT_KEY] as Long
			}
		}
		VLog.d("On SendFragment Params : FingerPrint  ${curFingerPrint}, NetworkType : $curNetworkType")
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		val view = inflater.inflate(R.layout.fragment_send, container, false)
		VLog.d("OnCreateView on send Fragment")
		return view
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		registerCLicks()
		getQrCodeDecoded()
		queryWalletList(curNetworkType, curFingerPrint)
		updateNetworkName(curNetworkType)
		initTxtAboveEdits()
		initMoveFocusToEnterAmount()
		determineOneWalletShow()
		VLog.d("OnViewCreated on send Fragment")
	}

	private fun removeGADIfCurNetworkChives(networkType: String) {
		if (isThisNotChiaNetwork(networkType)) {
			binding.txtAmountInGAD.visibility = View.GONE
			binding.txtGAD.visibility = View.GONE
		} else {
			binding.txtAmountInGAD.visibility = View.VISIBLE
			binding.txtGAD.visibility = View.VISIBLE
		}
		val enterAmount = binding.edtEnterAmount.text.toString().toDoubleOrNull()
		if (enterAmount != null) {
			convertAmountToUSDGAD(enterAmount)
		}
	}


	private fun initWebView() {
		val webView = binding.webViewToSend
		val settings = binding.webViewToSend.settings
		settings.javaScriptEnabled = true
		settings.allowUniversalAccessFromFileURLs = true
		webView.webViewClient = CallBack()
		webView.webChromeClient = MyWebChromeClient()
		webView.loadUrl("file:///android_asset/just.html")
	}

	private class MyWebChromeClient : WebChromeClient() {
		override fun onJsAlert(
			view: WebView,
			url: String,
			message: String,
			result: JsResult
		): Boolean {
			Log.d("LogTag MainActivity", message)
			result.confirm()
			return true
		}
	}


	private inner class CallBack : WebViewClient() {

		override fun shouldOverrideUrlLoading(
			view: WebView?,
			request: WebResourceRequest?
		): Boolean {
			return false
		}

		override fun onPageFinished(view: WebView, url: String?) {
			super.onPageFinished(view, url)
			view.loadUrl("javascript:alert(formParams())")
		}

	}

	private fun formParamsForSendingInsideWebView(address: String) {
		val webObj = WebAppInterface()
		webObj.address = address
		binding.webViewToSend.addJavascriptInterface(webObj, "Android")
	}

	inner class WebAppInterface {

		var address = ""

		@JavascriptInterface
		fun getAddressForPuzzle(): String {
			return address
		}

		@JavascriptInterface
		fun getPuzzleHash(puzzle_hash: String) {
			VLog.d("Received Puzzle_Hash to Send Coins: ${puzzle_hash}")
			initFlutterToGenerateSpendBundle(puzzle_hash)
		}

		@JavascriptInterface
		fun errorConverting() {
			dialogManager.hideProgress()
			showFailedSendingTransaction()
		}

	}


	private fun regulateVisibilityOfTxtsAfterPasscode() {
		binding.apply {
			if (edtEnterAmount.text.toString().isNotEmpty()) {
				txtEnterAmount.visibility = View.VISIBLE
				txtShortNetworkType.text = "$chosenTokenCode"
			}
			if (edtAddressWallet.text.toString().isNotEmpty())
				txtEnterAddressWallet.visibility = View.VISIBLE
			if (edtEnterCommission.text.toString().isNotEmpty())
				txtEnterCommission.visibility = View.VISIBLE
			if (twoEdtsFilled.size >= 2) {
				btnContinue.isEnabled = true
			}
			VLog.d("EdtFilledSize : ${twoEdtsFilled.size}")
		}
	}

	private fun determineOneWalletShow() {
		if (curFingerPrint != null) {
			ic_wallet_list.visibility = View.INVISIBLE
			imgIconSpinner.visibility = View.INVISIBLE
			chosenNetworkRel.isEnabled = false
		} else {
			initNetworkAdapter()
		}
	}


	private fun queryWalletList(type: String, fingerPrint: Long?) {
		removeGADIfCurNetworkChives(type)
		updateJob?.cancel()
		updateJob = lifecycleScope.launch {

			val walletTokenList = viewModel.queryWalletWithTokensList(type, fingerPrint)

			for (walletWithToken in walletTokenList) {
				VLog.d("Wallet Token on SendFragment : $walletWithToken ")
			}

			walletAdapter = WalletListAdapter(curActivity(), walletTokenList)
			binding.walletSpinner.adapter = walletAdapter
			if (walletAdapterPosition < walletTokenList.size)
				binding.walletSpinner.setSelection(walletAdapterPosition)
			binding.walletSpinner.onItemSelectedListener =
				object : AdapterView.OnItemSelectedListener {
					override fun onItemSelected(
						p0: AdapterView<*>?,
						p1: View?,
						p2: Int,
						p3: Long
					) {
						VLog.d("Select In Wallet Adapter : $p2")
						walletAdapter.selectedPosition = p2
						walletAdapterPosition = p2
						initCurWalletDetails(walletAdapter.walletList[p2])
					}

					override fun onNothingSelected(p0: AdapterView<*>?) {

					}

				}
		}
	}

	private fun initMoveFocusToEnterAmount() {
		binding.edtAddressWallet.setOnEditorActionListener(object :
			TextView.OnEditorActionListener {
			override fun onEditorAction(p0: TextView?, p1: Int, p2: KeyEvent?): Boolean {
				if (p1 == EditorInfo.IME_ACTION_DONE) {
					binding.edtEnterAmount.requestFocus()
					return true
				}
				return false
			}
		})
	}

	@SuppressLint("SetTextI18n")
	private fun initCurWalletDetails(wallet: WalletWithTokens) {
		txt_network_name.text = wallet.networkType.split(" ")[0]
		txtHiddenPublicKey.text = hidePublicKey(wallet.fingerPrint)

		updateTokensSpinner(wallet.tokenWalletList)
	}

	private fun updateTokensSpinner(tokenWalletList: List<TokenWallet>) {
		if (tokenWalletList.size == 1) {
			ic_token_downward.visibility = View.GONE
		} else {
			ic_token_downward.visibility = View.VISIBLE
		}
		chosenTokenWalletList = tokenWalletList
		tokenAdapter = NetworkAdapter(curActivity(), tokenWalletList.map { it.code }.toList())
		binding.tokenSpinner.adapter = tokenAdapter
		binding.icTokenDownward.setOnClickListener {
			binding.tokenSpinner.performClick()
		}

		binding.tokenSpinner.onItemSelectedListener =
			object : AdapterView.OnItemSelectedListener {
				override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
					tokenAdapter.selectedPosition = p2
					updateAmounts(tokenWalletList[p2])
				}

				override fun onNothingSelected(p0: AdapterView<*>?) {

				}
			}
	}

	@SuppressLint("SetTextI18n")
	private fun updateAmounts(tokenWallet: TokenWallet) {
		txtWalletAmount.text =
			formattedDoubleAmountWithPrecision(tokenWallet.amount) + " ${tokenWallet.code}"
		val formattedBalance = String.format("%.2f", tokenWallet.amountInUSD).replace(",", ".")
		txtWalletAmountInUsd.text = "⁓$formattedBalance USD"
		availableAmount = tokenWallet.amount
		chosenTokenCode = tokenWallet.code
		if (txtShortNetworkType.text.toString().length > 1) {
			txtShortNetworkType.text = tokenWallet.code
		}
		if (binding.enterCommissionToken.text.toString().length > 1) {
			binding.enterCommissionToken.text = tokenWallet.code
		}
	}


	private fun initTxtAboveEdits() {
		binding.apply {

			edtAddressWallet.setOnFocusChangeListener { view, focus ->
				if (focus) {
					txtEnterAddressWallet.visibility = View.VISIBLE
					edtAddressWallet.hint = ""
					line2.setBackgroundColor(curActivity().getColorResource(R.color.green))
				} else if (edtAddressWallet.text.toString().isEmpty()) {
					txtEnterAddressWallet.visibility = View.INVISIBLE
					edtAddressWallet.hint = curActivity().getString(R.string.send_token_adress)
				}
				if (!focus) {
					line2.setBackgroundColor(curActivity().getColorResource(R.color.edt_divider))
				}
			}


			edtAddressWallet.addTextChangedListener {
				if (it == null) return@addTextChangedListener
				if (!it.contains("."))
					curSendAddressWallet = it.toString()
			}


			edtEnterAmount.setOnFocusChangeListener { view, focus ->
				if (focus) {
					txtEnterAmount.visibility = View.VISIBLE
					edtEnterAmount.hint = ""
					view2.setBackgroundColor(curActivity().getColorResource(R.color.green))
					txtShortNetworkType.apply {
						setTextColor(curActivity().getColorResource(R.color.green))
						text = chosenTokenCode
					}
				} else if (edtEnterAmount.text.toString().isEmpty()) {
					txtEnterAmount.visibility = View.INVISIBLE
					edtEnterAmount.hint = curActivity().getString(R.string.send_token_amount)
					txtShortNetworkType.apply {
						text = "-"
						setTextColor(curActivity().getColorResource(R.color.txtShortNetworkType))
					}
				}
				if (!focus) {
					view2.setBackgroundColor(curActivity().getColorResource(R.color.edt_divider))
				}
			}

			edtEnterCommission.setOnFocusChangeListener { view, focus ->
				if (focus) {
					txtEnterCommission.visibility = View.VISIBLE
					edtEnterCommission.hint = ""
					view3.setBackgroundColor(curActivity().getColorResource(R.color.green))
					enterCommissionToken.apply {
						setTextColor(curActivity().getColorResource(R.color.green))
						text = getShortNetworkType(curNetworkType)
					}
					txtRecommendedCommission.visibility = View.VISIBLE
				} else if (edtEnterCommission.text.toString().isEmpty()) {
					txtEnterCommission.visibility = View.INVISIBLE
					edtEnterCommission.hint =
						curActivity().getString(R.string.send_token_commission_amount)
					enterCommissionToken.apply {
						text = "-"
						setTextColor(curActivity().getColorResource(R.color.txtShortNetworkType))
					}
					txtRecommendedCommission.visibility = View.INVISIBLE
				}
				if (!focus) {
					view3.setBackgroundColor(curActivity().getColorResource(R.color.edt_divider))
				}
			}
		}
	}

	private fun initNetworkAdapter() {

		lifecycleScope.launch {

			val distinctNetworkTypes =
				viewModel.getDistinctNetworkTypeValues()

			networkAdapter =
				NetworkAdapter(curActivity(), distinctNetworkTypes)
			binding.networkSpinner.adapter = networkAdapter

			binding.networkSpinner.setSelection(distinctNetworkTypes.indexOf(curNetworkType))

			binding.networkSpinner.onItemSelectedListener =
				object : AdapterView.OnItemSelectedListener {

					override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
						VLog.d("Selected item position : $p2")
						updateNetworkName(networkAdapter.dataOptions[p2])
						curNetworkType = networkAdapter.dataOptions[p2]
						networkAdapter.selectedPosition = p2
						queryWalletList(
							networkAdapter.dataOptions[p2],
							curFingerPrint
						)
					}

					override fun onNothingSelected(p0: AdapterView<*>?) {

					}

				}
		}
	}

	private var passcodeJob: Job? = null

	private fun waitingResponseEntPassCodeFragment() {
		passcodeJob?.cancel()
		passcodeJob = lifecycleScope.launch {
			curActivity().mainViewModel.money_send_success.collect {
				if (it) {
					VLog.d("Success entering the passcode : $it")
					delay(1000)
					getPuzzleHashFromAddress()
					curActivity().mainViewModel.send_money_false()
				}
			}
		}
	}

	private fun getPuzzleHashFromAddress() {
		dialogManager.showProgress(curActivity())
		initWebView()
		formParamsForSendingInsideWebView(curSendAddressWallet)
	}


	private fun sendTransaction(spendBundleJSON: String, url: String) {
		sendTransJob?.cancel()
		sendTransJob = lifecycleScope.launch {
			val res = viewModel.push_transaction(spendBundleJSON, url)
			when (res.state) {
				Resource.State.SUCCESS -> {
					dialogManager.hideProgress()
					insertAddressEntityIfBoxChecked()
					showSuccessSendMoneyDialog()
				}
				Resource.State.ERROR -> {
					dialogManager.hideProgress()
					showFailedSendingTransaction()
				}
				Resource.State.LOADING -> {

				}
			}

		}
	}

	private fun initFlutterToGenerateSpendBundle(
		dest_puzzle_hash: String
	) {
		genSpendBundleJob?.cancel()
		genSpendBundleJob = lifecycleScope.launch {
			val wallet = walletAdapter.walletList[walletAdapter.selectedPosition]
			val precision =
				if (isThisNotChiaNetwork(wallet.networkType)) Math.pow(
					10.0,
					8.0
				).toLong() else Math.pow(
					10.0,
					12.0
				).toLong()
			val amount = (getDoubleValueFromEdt(binding.edtEnterAmount) * precision).toLong()
			val fee = (getDoubleValueFromEdt(binding.edtEnterCommission) * precision).toLong()
			val mnemonicString = mnemonicsToString(wallet.mnemonics)
			val url = getNetworkItemFromPrefs(wallet.networkType)!!.full_node
			val methodChannel = MethodChannel(
				(curActivity().application as App).flutterEngine.dartExecutor.binaryMessenger,
				METHOD_CHANNEL_GENERATE_HASH
			)
			listenToCreatingSpendBundleRes(url)
			val argSpendBundle = hashMapOf<String, Any>()
			argSpendBundle["fee"] = fee
			argSpendBundle["amount"] = amount
			argSpendBundle["mnemonicString"] = mnemonicString
			argSpendBundle["url"] = url
			argSpendBundle["dest"] = dest_puzzle_hash
			argSpendBundle["is_main_net"] =
				!wallet.networkType.lowercase().contains("test")
			VLog.d("Body From Sending Fragment to flutter : $argSpendBundle ")
			methodChannel.invokeMethod("createSpendBundle", argSpendBundle)
		}
	}

	private suspend fun listenToCreatingSpendBundleRes(url: String) {
		withContext(Dispatchers.Main) {
			val methodChannel = MethodChannel(
				(curActivity().application as App).flutterEngine.dartExecutor.binaryMessenger,
				METHOD_CHANNEL_GENERATE_HASH
			)
			methodChannel.setMethodCallHandler { method, callBack ->
				if (method.method == "getSpendBundle") {
					val spendBundleJson =
						(method.arguments as HashMap<*, *>)["spendBundle"].toString()
					VLog.d("Got spend bundle on send fragment : $spendBundleJson")
					sendTransaction(spendBundleJson, url)
				}
			}
		}
	}

	private suspend fun getNetworkItemFromPrefs(networkType: String): NetworkItem? {
		val item = prefsManager.getObjectString(getPreferenceKeyForNetworkItem(networkType))
		if (item.isEmpty()) return null
		return gson.fromJson(item, NetworkItem::class.java)
	}

	private suspend fun insertAddressEntityIfBoxChecked() {
		if (binding.checkBoxAddAddress.isChecked) {
			val name = binding.edtAddressName.text.toString()
			val existAddress = viewModel.checkIfAddressExistInDb(curSendAddressWallet)
			if (existAddress.isEmpty())
				viewModel.insertAddressEntity(Address(curSendAddressWallet, name, ""))
		}
	}

	private fun getDoubleValueFromEdt(edt: EditText): Double {
		return edt.text.toString().toDoubleOrNull() ?: 0.0
	}

	private fun showSuccessSendMoneyDialog() {
		curActivity().apply {
			dialogManager.showSuccessDialog(
				this,
				getStringResource(R.string.send_token_pop_up_succsess_title),
				getStringResource(R.string.send_token_pop_up_succsess_description),
				getStringResource(R.string.ready_btn)
			) {
				curActivity().popBackStackOnce()
			}
		}
	}

	private fun showFailedSendingTransaction() {
		curActivity().apply {
			dialogManager.showFailureDialog(
				this,
				getStringResource(R.string.pop_up_failed_error_title),
				getStringResource(R.string.send_token_pop_up_transaction_fail_error_description),
				getStringResource(R.string.pop_up_failed_error_return_btn)
			) {

			}
		}
	}


	private fun getQrCodeDecoded() {
		lifecycleScope.launchWhenStarted {
			launch {
				curActivity().mainViewModel.decodedQrCode.collect {
					if (it.isNotEmpty()) {
						binding.edtAddressWallet.requestFocus()
						curSendAddressWallet = it
						kotlin.runCatching {
							val truncatedStr =
								it.substring(0, 20) + "..." + it.substring(it.length - 4, it.length)
							binding.edtAddressWallet.setText(truncatedStr)
						}.onFailure {
							binding.edtAddressWallet.setText(it.toString())
						}
					}
				}
			}
			curActivity().mainViewModel.chosenAddress.collect { addres ->
				VLog.d("Chosen Address from AddressFragment : $addres")
				if (addres.isNotEmpty()) {
					binding.edtAddressWallet.requestFocus()
					curSendAddressWallet = addres
					kotlin.runCatching {
						val truncatedStr =
							addres.substring(0, 20) + "..." + addres.substring(
								addres.length - 4,
								addres.length
							)
						binding.edtAddressWallet.setText(truncatedStr)
					}.onFailure {
						binding.edtAddressWallet.setText(addres)
					}
				}
			}
		}
	}


	private fun registerCLicks() {
		binding.apply {
			checkBoxAddAddress.setOnCheckedChangeListener(object :
				CompoundButton.OnCheckedChangeListener {
				override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
					addNameAddressLay.visibility = if (p1) View.VISIBLE else View.GONE
					txtAddressAlredyExistWarning.visibility = if (p1) View.VISIBLE else View.GONE
					checkBoxAddAddress.setTextColor(
						if (p1) ContextCompat.getColor(
							curActivity(),
							R.color.green
						) else ContextCompat.getColor(curActivity(), R.color.checkbox_text_color)
					)
					if (p1)
						checkAddressAlreadyExistInDB()
				}
			})
		}


		binding.backLayout.setOnClickListener {
			curActivity().popBackStackOnce()
		}


		binding.imgEdtScan.setOnClickListener {
			it.startAnimation(anim.getBtnEffectAnimation())
			requestPermissions.launch(arrayOf(android.Manifest.permission.CAMERA))
		}

		binding.imgAddressIc.setOnClickListener {
			curActivity().move2AddressFragmentList(true)
		}



		binding.edtEnterAmount.addTextChangedListener {
			kotlin.runCatching {
				if (it.isNullOrEmpty()) {
					twoEdtsFilled.remove(2)
					convertAmountToUSDGAD(0.0)
				} else {
					getCommissionOfCurChosenCoin()
					convertAmountToUSDGAD(it.toString().toDouble())
					val enteredAmount = it.toString().toDouble()
					val availableAmount = availableAmount
					if (enteredAmount > availableAmount) {
						twoEdtsFilled.remove(2)
						showNotEnoughAmountWarning()
					} else {
						twoEdtsFilled.add(2)
					}

				}
				enableBtnContinueThreeEdtsFilled()
			}.onFailure {
				VLog.d("Excepting entering amount into amoung edts : ${it}")
			}
		}
		binding.edtAddressWallet.addTextChangedListener {
			if (it.isNullOrEmpty()) {
				twoEdtsFilled.remove(1)
			} else {
				twoEdtsFilled.add(1)
			}
			enableBtnContinueThreeEdtsFilled()
		}

		binding.btnContinue.setOnClickListener {
			binding.apply {
				val curSum = edtEnterAmount.text.toString().toDoubleOrNull()
				val addressWalletInValid = edtAddressWallet.text.toString().isEmpty()
				if (addressWalletInValid) {
					showWarningInvalidWalledAddress()
					return@setOnClickListener
				}
				showConfirmTransactionDialog()
			}
		}

		ic_wallet_list.setOnClickListener {
			binding.walletSpinner.performClick()
		}


		chosenNetworkRel.setOnClickListener {
			VLog.d("NetworkRelative is clicked")
			binding.networkSpinner.performClick()
		}

		curActivity().mainViewModel.send_money_false()

		anim.animateArrowIconCustomSpinner(token_spinner, ic_token_downward)
		anim.animateArrowIconCustomSpinner(wallet_spinner, ic_wallet_list)
		anim.animateArrowIconCustomSpinner(network_spinner, imgIconSpinner)

	}

	private fun convertAmountToUSDGAD(amount: Double) {
		lifecycleScope.launch {
			val amountInUSD = amount * viewModel.getCourseCurrencyCoin(curNetworkType)
			binding.txtAmountInUSD.setText("⁓${formattedDoubleAmountWithPrecision(amountInUSD)}")
			val gadPrice = viewModel.getTokenPriceByCode("GAD")
			var convertedAmountGAD = 0.0
			if (gadPrice != 0.0) {
				convertedAmountGAD = amountInUSD / gadPrice
			}
			binding.txtAmountInGAD.setText("~${formattedDoubleAmountWithPrecision(convertedAmountGAD)}")
		}
	}

	private fun checkAddressAlreadyExistInDB() {
		addressAlreadyExist?.cancel()
		addressAlreadyExist = lifecycleScope.launch {
			val addressList = viewModel.checkIfAddressExistInDb(curSendAddressWallet)
			VLog.d("AddressList by given address :  ${addressList.size}")
			if (addressList.isNotEmpty()) {
				txtAddressAlredyExistWarning.visibility = View.VISIBLE
			} else {
				txtAddressAlredyExistWarning.visibility = View.GONE
			}
		}
	}


	private fun showWarningInvalidWalledAddress() {
		binding.apply {
			edtAddressWallet.setTextColor(curActivity().getColorResource(R.color.red_mnemonic))
			txtEnterAddressWallet.setTextColor(curActivity().getColorResource(R.color.red_mnemonic))
			txtAddressDontExistWarning.visibility = View.VISIBLE
			lifecycleScope.launch {
				delay(2000)
				edtAddressWallet.setTextColor(curActivity().getColorResource(R.color.secondary_text_color))
				txtEnterAddressWallet.setTextColor(curActivity().getColorResource(R.color.green))
				txtAddressDontExistWarning.visibility = View.GONE
			}
		}
	}

	private fun showNotEnoughAmountWarning() {
		binding.apply {
			edtEnterAmount.setTextColor(curActivity().getColorResource(R.color.red_mnemonic))
			txtEnterAmount.setTextColor(curActivity().getColorResource(R.color.red_mnemonic))
			txtNotEnoughMoneyWarning.visibility = View.VISIBLE
			lifecycleScope.launch {
				delay(2000)
				edtEnterAmount.setTextColor(curActivity().getColorResource(R.color.secondary_text_color))
				txtEnterAmount.setTextColor(curActivity().getColorResource(R.color.green))
				txtNotEnoughMoneyWarning.visibility = View.GONE
			}
		}
	}


	private fun getCommissionOfCurChosenCoin() {
		commissionJob?.cancel()
		commissionJob = lifecycleScope.launch(handler) {
			val commission =
				viewModel.getCoinDetailsFeeCommission(getShortNetworkType(curNetworkType))
			binding.edtEnterCommission.setText("$commission")
		}
	}

	@SuppressLint("SetTextI18n")
	private fun updateNetworkName(coinType: String) {
		VLog.d("Selected CoinType : $coinType")
		binding.apply {
			txtChosenNetwork.text = coinType
		}
	}

	private fun enableBtnContinueThreeEdtsFilled() {
		binding.btnContinue.isEnabled = twoEdtsFilled.size >= 2
	}

	private fun showConfirmTransactionDialog() {
		val dialog = Dialog(requireActivity(), R.style.RoundedCornersDialog)
		dialog.setContentView(R.layout.dialog_confirm_transactions)
		registerBtnClicks(dialog)
		initConfirmDialogDetails(dialog)
		val width = resources.displayMetrics.widthPixels
		dialog.apply {
			addingDoubleDotsTxt(txtToken)
			addingDoubleDotsTxt(txtBlockChain)
			addingDoubleDotsTxt(txtAmount)
			addingDoubleDotsTxt(txtAddress)
		}
		dialog.window?.setLayout(
			width,
			WindowManager.LayoutParams.WRAP_CONTENT
		)
		dialog.show()
	}

	private fun initConfirmDialogDetails(dialog: Dialog) {
		dialog.apply {
			findViewById<TextView>(R.id.edtConfirmToken).setText(tokenAdapter.dataOptions[tokenAdapter.selectedPosition])
			findViewById<TextView>(R.id.edtConfirmNetwork).setText(walletAdapter.walletList[walletAdapter.selectedPosition].networkType)
			findViewById<TextView>(R.id.edtConfirmWalletAmount).setText(binding.edtEnterAmount.text.toString())
			findViewById<TextView>(R.id.edtConfirmAddressWallet).setText(binding.edtAddressWallet.text.toString())
		}
	}


	@SuppressLint("SetTextI18n")
	private fun addingDoubleDotsTxt(txt: TextView) {
		txt.apply {
			txt.text = "${txt.text}:"
		}
	}

	private fun registerBtnClicks(dialog: Dialog) {
		dialog.findViewById<Button>(R.id.btnConfirm).setOnClickListener {
			it.startAnimation(anim.getBtnEffectAnimation())
			dialog.dismiss()
			curActivity().showEnterPasswordFragment(reason = ReasonEnterCode.SEND_MONEY)
			waitingResponseEntPassCodeFragment()
		}
		dialog.findViewById<LinearLayout>(R.id.back_layout).setOnClickListener {
			dialog.dismiss()
		}
	}


	override fun onStart() {
		super.onStart()
		VLog.d("Send Fragment on Start")
		regulateVisibilityOfTxtsAfterPasscode()
	}

	override fun onResume() {
		super.onResume()
		VLog.d("SendFragment onResume")
	}


	override fun onPause() {
		super.onPause()
		VLog.d("SendFragment onPause")
	}

	override fun onStop() {
		super.onStop()
		VLog.d("SendFragment onStop")
	}


	private val requestPermissions =
		registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { it ->
			it.entries.forEach {
				if (it.value) {
					curActivity().mainViewModel.saveDecodeQrCode("")
					curActivity().move2ScannerFragment(null, null)
				} else
					Toast.makeText(
						curActivity(),
						"Разрешение нету для камера",
						Toast.LENGTH_SHORT
					)
						.show()
			}
		}


	override fun onDestroyView() {
		super.onDestroyView()
	}

	override fun onDestroy() {
		super.onDestroy()
		sendTransJob?.cancel()
	}

	private fun curActivity() = requireActivity() as MainActivity

}
