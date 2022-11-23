package com.android.greenapp.presentation.main.send

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Paint
import android.os.Bundle
import android.text.InputFilter
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
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
import com.android.greenapp.domain.domainmodel.Address
import com.android.greenapp.domain.domainmodel.TokenWallet
import com.android.greenapp.domain.domainmodel.WalletWithTokens
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
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap


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

	@Inject
	lateinit var connectionLiveData: ConnectionLiveData

	private var updateJob: Job? = null
	private var sendTransJob: Job? = null
	private var addressAlreadyExist: Job? = null
	private var commissionJob: Job? = null
	private var genSpendBundleJob: Job? = null


	private var curSendAddressWallet: String = ""
	private var spendableAmountToken: Double = 0.0
	private var spendableAmountFee: Double = 0.0
	private var chosenTokenCode: String = ""
	private var walletAdapterPosition = 0
	private var tokendAdapterPosition = 0

	private val handler = CoroutineExceptionHandler { _, ex ->
		VLog.d("Exception caught on send fragment : ${ex.message} ")
	}

	private var curTokenWalletList = listOf<TokenWallet>()
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
		curActivity().sendCoinsFragmentView = view
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
		initSwipeRefreshLayout()
	}

	private fun initConstrainsAfterCommobasedOnToken() {
		val sendingToken = tokenAdapter.dataOptions[tokenAdapter.selectedPosition]
		val commissionToken = tokenAdapter.dataOptions[0]
		val precisionAfterCommonToken = getTokenPrecisionAfterComoByTokenCode(sendingToken)
		val precisionAfterCommonFee = getTokenPrecisionAfterComoByTokenCode(commissionToken)
		val filterSendingAmountEdt = object : InputFilter {
			override fun filter(
				p0: CharSequence?,
				p1: Int,
				p2: Int,
				p3: Spanned?,
				p4: Int,
				p5: Int
			): CharSequence {
				if (p0 == null) return ""
				val curText = binding.edtEnterAmount.text.toString()
				val locComo = curText.indexOf('.')
				if (locComo == -1 || locComo == 0)
					return p0
				val digitsAfterComo = curText.substring(locComo + 1, curText.length).length
				if (digitsAfterComo >= precisionAfterCommonToken) {
					return ""
				}
				return p0
			}
		}
		val filterFeeAmountEdt = object : InputFilter {
			override fun filter(
				p0: CharSequence?,
				p1: Int,
				p2: Int,
				p3: Spanned?,
				p4: Int,
				p5: Int
			): CharSequence {
				if (p0 == null) return ""
				val curText = binding.edtEnterCommission.text.toString()
				val locComo = curText.indexOf('.')
				if (locComo == -1 || locComo == 0)
					return p0
				val digitsAfterComo = curText.substring(locComo + 1, curText.length).length
				if (digitsAfterComo >= precisionAfterCommonFee) {
					return ""
				}
				return p0
			}
		}
		binding.apply {
			edtEnterAmount.filters = arrayOf(filterSendingAmountEdt)
			edtEnterCommission.filters = arrayOf(filterFeeAmountEdt)
		}
	}

	private var swipeJob: Job? = null

	private fun initSwipeRefreshLayout() {
		binding.swipeRefresh.apply {
			setOnRefreshListener {
				swipeJob?.cancel()
				swipeJob = lifecycleScope.launch {
					val job = launch {
						viewModel.swipedRefreshLayout {
							VLog.d("Is refreshing false on send fragment :")
						}
					}
					job.join()
					isRefreshing = false
				}
			}
			setColorSchemeResources(R.color.green)
		}
	}

	@SuppressLint("SetTextI18n")
	private fun calculateSpendableBalance() {
		lifecycleScope.launch(handler) {
			val curSendingToken = tokenAdapter.dataOptions[tokenAdapter.selectedPosition]
			val curWallet = walletAdapter.walletList[walletAdapter.selectedPosition]
			withContext(Dispatchers.IO) {
				val sentTokenMempoolAmounts =
					viewModel.getSpentCoinsAmountsAddressCodeForSpendableBalance(
						address = curWallet.address,
						tokenCode = curSendingToken,
						networkType = curWallet.networkType
					)
//				VLog.d("Address : ${curWallet.address} , SentToken : $curSendingToken  Amounts : ${sentTokenMempoolAmounts.toList()}")
				withContext(Dispatchers.Main) {
					val initialAmountToken =
						curWallet.tokenWalletList[tokenAdapter.selectedPosition].amount
					val initialAmountNetworkTypeToken =
						curWallet.tokenWalletList[0].amount
					val txtSpendableBalance =
						curActivity().getStringResource(R.string.spendable_balance)
					spendableAmountToken = initialAmountToken - sentTokenMempoolAmounts[0]
					spendableAmountFee =
						initialAmountNetworkTypeToken - sentTokenMempoolAmounts[1]
					val bigDecimalSpendableAmount =
						(BigDecimal("$initialAmountToken").subtract(BigDecimal("${sentTokenMempoolAmounts[0]}"))).toDouble()

					var spendableAmountString =
						formattedDoubleAmountWithPrecision(bigDecimalSpendableAmount)
					if (Math.round(bigDecimalSpendableAmount)
							.toDouble() == bigDecimalSpendableAmount
					)
						spendableAmountString = "${Math.round(bigDecimalSpendableAmount)}"
					val bigDecimalSpendableFee =
						formattedDoubleAmountWithPrecision(
							(BigDecimal("$initialAmountNetworkTypeToken").subtract(
								BigDecimal("${sentTokenMempoolAmounts[1]}")
							)).toDouble()
						)
					binding.apply {
						txtSpendableBalanceAmount.setText(
							"$txtSpendableBalance: $spendableAmountString"
						)
						txtSpendableBalanceCommission.setText(
							"$txtSpendableBalance: $bigDecimalSpendableFee"
						)
					}
					checkBtnEnabledAfterTokenChanged()
					initConstrainsAfterCommobasedOnToken()
				}
			}
		}
	}

	private fun removeGADIfCurNetworkChives(networkType: String) {
		if (isThisChivesNetwork(networkType)) {
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
		verifySendingAddress(wallet.address)
	}

	private fun updateTokensSpinner(tokenWalletList: List<TokenWallet>) {
		if (tokenWalletList.size == 1) {
			ic_token_downward.visibility = View.GONE
		} else {
			ic_token_downward.visibility = View.VISIBLE
		}
		curTokenWalletList = tokenWalletList
		tokenAdapter = NetworkAdapter(curActivity(), tokenWalletList.map { it.code }.toList())
		binding.tokenSpinner.adapter = tokenAdapter
		binding.icTokenDownward.setOnClickListener {
			binding.tokenSpinner.performClick()
		}
		if (tokendAdapterPosition < tokenWalletList.size)
			binding.tokenSpinner.setSelection(tokendAdapterPosition)
		binding.tokenSpinner.onItemSelectedListener =
			object : AdapterView.OnItemSelectedListener {
				override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
					tokenAdapter.selectedPosition = p2
					tokendAdapterPosition = p2
					calculateSpendableBalance()
					updateAmounts(tokenWalletList[p2])
				}

				override fun onNothingSelected(p0: AdapterView<*>?) {

				}
			}

	}

	private fun checkBtnEnabledAfterTokenChanged() {
		val sendingAmount = binding.edtEnterAmount.text.toString().toDoubleOrNull() ?: 0.0
		if (sendingAmount > spendableAmountToken) {
			twoEdtsFilled.remove(2)
			showNotEnoughAmountWarning()
			notEnoughAmountWarningSending()
		} else {
			twoEdtsFilled.add(2)
			hideAmountNotEnoughWarning()
			hideNotEnoughAmountWarningSending()
			val totalWithCommission = getCommissionAmount() + getSendingAmountForFee()
//				VLog.d("TotalWithCommission on checkBtnEnabled : $totalWithCommission : Commission : ${getCommissionAmount()} and EnteredAmount : ${getSendingAmountForFee()}")
			if (totalWithCommission > spendableAmountFee) {
				twoEdtsFilled.remove(2)
				showNotEnoughAmountWarning()
				if (tokenAdapter.selectedPosition == 0) {
					notEnoughAmountWarningSending()
				} else {
					hideNotEnoughAmountWarningSending()
				}
			} else {
				twoEdtsFilled.add(2)
				hideAmountNotEnoughWarning()
				hideNotEnoughAmountWarningFee()
			}
		}
		enableBtnContinueTwoEdtsFilled()
	}

	@SuppressLint("SetTextI18n")
	private fun updateAmounts(tokenWallet: TokenWallet) {
		txtWalletAmount.text =
			formattedDoubleAmountWithPrecision(tokenWallet.amount) + " ${tokenWallet.code}"
		val formattedBalance = String.format("%.2f", tokenWallet.amountInUSD).replace(",", ".")
		txtWalletAmountInUsd.text = "⁓$formattedBalance USD"
		chosenTokenCode = tokenWallet.code
		if (txtShortNetworkType.text.toString().length > 1) {
			txtShortNetworkType.text = tokenWallet.code
		}
		if (binding.enterCommissionToken.text.toString().length > 1) {
			binding.enterCommissionToken.text = tokenAdapter.dataOptions[0]
		}
		if (tokenAdapter.selectedPosition != 0) {
			binding.txtGAD.text = "XCH"
		} else {
			binding.txtGAD.text = "GAD"
		}
		if (binding.edtEnterAmount.text.toString().isNotEmpty()) {
			val amount = binding.edtEnterAmount.text.toString().toDouble()
			convertAmountToUSDGAD(amount)
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
			}


			edtEnterAmount.setOnFocusChangeListener { view, focus ->
				if (focus) {
					txtEnterAmount.visibility = View.VISIBLE
					txtSpendableBalanceAmount.visibility = View.VISIBLE
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
					txtSpendableBalanceAmount.visibility = View.GONE
				}
				if (!focus) {
					view2.setBackgroundColor(curActivity().getColorResource(R.color.edt_divider))
				}
			}

			edtEnterCommission.setOnFocusChangeListener { view, focus ->
				if (focus) {
					txtEnterCommission.visibility = View.VISIBLE
					txtSpendableBalanceCommission.visibility = View.VISIBLE
					edtEnterCommission.hint = ""
					view3.setBackgroundColor(curActivity().getColorResource(R.color.green))
					enterCommissionToken.apply {
						setTextColor(curActivity().getColorResource(R.color.green))
						text = getShortNetworkType(curNetworkType)
					}
					txtRecommendedCommission.visibility = View.VISIBLE
					changeCommissionValueFromRest()
				} else if (edtEnterCommission.text.toString().isEmpty()) {
					txtEnterCommission.visibility = View.INVISIBLE
					edtEnterCommission.hint =
						curActivity().getString(R.string.send_token_commission_amount)
					enterCommissionToken.apply {
						text = "-"
						setTextColor(curActivity().getColorResource(R.color.txtShortNetworkType))
					}
					txtRecommendedCommission.visibility = View.INVISIBLE
					txtSpendableBalanceCommission.visibility = View.GONE
				}
				if (!focus) {
					view3.setBackgroundColor(curActivity().getColorResource(R.color.edt_divider))
				}
			}


			txtRecommendedCommission.setOnClickListener {
				edtEnterCommission.setText(txtRecommendedCommission.text.toString().trim())
			}
			view3.setOnClickListener {
				edtEnterCommission.setText(txtRecommendedCommission.text.toString().trim())
			}

		}
	}

	@SuppressLint("SetTextI18n")
	private fun changeCommissionValueFromRest() {
		lifecycleScope.launch {
			val commission =
				viewModel.getCoinDetailsFeeCommission(getShortNetworkType(curNetworkType))

			val text = formattedDoubleAmountWithPrecision(commission)
			val ss = SpannableString(text)
			val fcsGreen = ForegroundColorSpan(resources.getColor(R.color.green))
			val underlineSpan = UnderlineSpan()
			ss.setSpan(fcsGreen, 0, text.length, SpannableString.SPAN_INCLUSIVE_INCLUSIVE)
			ss.setSpan(underlineSpan, 0, text.length, SpannableString.SPAN_INCLUSIVE_INCLUSIVE)
			binding.txtRecommendedCommission.apply {
				setText("$ss")
				movementMethod = LinkMovementMethod.getInstance()
				paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
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
					delay(500)
					dialogManager.showProgress(curActivity())
					initFlutterToGenerateSpendBundle(binding.edtAddressWallet.text.toString())
					curActivity().mainViewModel.send_money_false()
				}
			}
		}
	}


	private fun sendTransaction(
		spendBundleJSON: String,
		url: String,
		amount: Double,
		networkType: String,
		fingerPrint: Long,
		dest_puzzle_hash: String,
		address: String,
		spentCoinsJson: String,
		spentConisToken: String
	) {
		sendTransJob?.cancel()
		sendTransJob = lifecycleScope.launch {
			val res =
				viewModel.push_transaction(
					spendBundleJSON,
					url,
					amount,
					networkType,
					fingerPrint,
					chosenTokenCode,
					dest_puzzle_hash,
					address = address,
					getDoubleValueFromEdt(binding.edtEnterCommission),
					spentCoinsJson,
					spentConisToken
				)
			when (res.state) {
				Resource.State.SUCCESS -> {
					dialogManager.hidePrevDialogs()
					insertAddressEntityIfBoxChecked()
					showSuccessSendMoneyDialog()
				}
				Resource.State.ERROR -> {
					showFailedSendingTransaction()
				}
				Resource.State.LOADING -> {

				}
			}

		}
	}

	private fun initFlutterToGenerateSpendBundle(
		dest_address: String
	) {
		genSpendBundleJob?.cancel()
		genSpendBundleJob = lifecycleScope.launch(handler) {
			val wallet = walletAdapter.walletList[walletAdapter.selectedPosition]
			val asset_id = curTokenWalletList[tokenAdapter.selectedPosition].asset_id
			val curCode = curTokenWalletList[tokenAdapter.selectedPosition].code
			var precision =
				if (isThisChivesNetwork(wallet.networkType)) Math.pow(
					10.0,
					8.0
				).toLong() else Math.pow(
					10.0,
					12.0
				).toLong()
			if (asset_id.isNotEmpty())
				precision = 1000
			val amount = (Math.round(getDoubleValueFromEdt(binding.edtEnterAmount) * precision))
			val fee =
				Math.round(
					getDoubleValueFromEdt(binding.edtEnterCommission) * if (isThisChivesNetwork(
							wallet.networkType
						)
					) Math.pow(
						10.0,
						8.0
					) else Math.pow(
						10.0,
						12.0
					)
				)
			val mnemonicString = convertListToStringWithSpace(wallet.mnemonics)
			val url = getNetworkItemFromPrefs(wallet.networkType)!!.full_node
			val alreadySpentCoins =
				viewModel.getSpentCoinsToPushTrans(wallet.networkType, wallet.address, curCode)
			val methodChannel = MethodChannel(
				(curActivity().application as App).flutterEngine.dartExecutor.binaryMessenger,
				METHOD_CHANNEL_GENERATE_HASH
			)
			listenToCreatingSpendBundleRes(
				url,
				getDoubleValueFromEdt(binding.edtEnterAmount),
				wallet.networkType,
				wallet.fingerPrint,
				wallet.address
			)
			val argSpendBundle = hashMapOf<String, Any>()
			argSpendBundle["fee"] = fee
			argSpendBundle["amount"] = amount
			argSpendBundle["mnemonicString"] = mnemonicString
			argSpendBundle["url"] = url
			argSpendBundle["dest"] = dest_address
			argSpendBundle["network_type"] = convertNetworkTypeForFlutter(wallet.networkType)
			argSpendBundle["asset_id"] = asset_id
			argSpendBundle["spentCoins"] = Gson().toJson(alreadySpentCoins)
			VLog.d("Body From Sending Fragment to flutter : $argSpendBundle")
			if (asset_id.isEmpty())
				methodChannel.invokeMethod("generateSpendBundleXCH", argSpendBundle)
			else {
				methodChannel.invokeMethod("generateSpendBundleToken", argSpendBundle)
			}
		}
	}

	private fun convertNetworkTypeForFlutter(networkType: String): String {
		val lowercaseNetwork = networkType.lowercase()
		if (lowercaseNetwork.contains("chia")) {
			if (lowercaseNetwork.contains("testnet"))
				return "Chia TestNet"
			return "Chia"
		}
		if (lowercaseNetwork.contains("testnet"))
			return "Chives TestNet"
		return "Chives"
	}

	private suspend fun listenToCreatingSpendBundleRes(
		url: String,
		amount: Double,
		networkType: String,
		fingerPrint: Long,
		address: String
	) {
		val methodChannel = MethodChannel(
			(curActivity().application as App).flutterEngine.dartExecutor.binaryMessenger,
			METHOD_CHANNEL_GENERATE_HASH
		)
		methodChannel.setMethodCallHandler { method, callBack ->
			if (method.method == "getSpendBundle") {
				val spendBundleJson =
					(method.arguments as HashMap<*, *>)["spendBundle"].toString()
				val dest_puzzle_hash =
					(method.arguments as HashMap<*, *>)["dest_puzzle_hash"].toString()
				val spentCoinsJson =
					(method.arguments as HashMap<*, *>)["spentCoins"].toString()
				val spentTokensJson = (method.arguments as HashMap<*, *>)["spentTokens"] ?: ""
				VLog.d("SpentCoins Json for sending trans : $spentCoinsJson")
				sendTransaction(
					spendBundleJson,
					url,
					amount,
					networkType,
					fingerPrint,
					dest_puzzle_hash,
					address,
					spentCoinsJson,
					spentTokensJson.toString()
				)
			} else if (method.method == "exception") {
				showFailedSendingTransaction()
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
			val existAddress =
				viewModel.checkIfAddressExistInDb(binding.edtAddressWallet.text.toString())
			if (existAddress.isEmpty())
				viewModel.insertAddressEntity(
					Address(
						UUID.randomUUID().toString(),
						binding.edtAddressWallet.text.toString(),
						name,
						"",
						System.currentTimeMillis()
					)
				)
		}
	}

	private fun getDoubleValueFromEdt(edt: EditText): Double {
		return edt.text.toString().toDoubleOrNull() ?: 0.0
	}

	private fun showSuccessSendMoneyDialog() {
		curActivity().apply {
			if (!this.isFinishing) {
				dialogManager.showSuccessDialog(
					this,
					getStringResource(R.string.send_token_pop_up_succsess_title),
					getStringResource(R.string.send_token_pop_up_succsess_description),
					getStringResource(R.string.ready_btn)
				) {
					popBackStackOnce()
				}
			}
		}

	}

	private fun showFailedSendingTransaction() {
		dialogManager.hidePrevDialogs()
		curActivity().apply {
			if (!this.isFinishing) {
				dialogManager.showFailureDialog(
					this,
					getStringResource(R.string.pop_up_failed_error_title),
					getStringResource(R.string.send_token_pop_up_transaction_fail_error_description),
					getStringResource(R.string.pop_up_failed_error_return_btn)
				) {

				}
			}
		}
	}

	private fun verifySendingAddress(address: String) {

	}


	private fun getQrCodeDecoded() {
		lifecycleScope.launchWhenStarted {
			launch {
				curActivity().mainViewModel.decodedQrCode.collect {
					if (it.isNotEmpty()) {
						binding.edtAddressWallet.setText(it)
					}
				}
			}
			curActivity().mainViewModel.chosenAddress.collect { addres ->
				VLog.d("Chosen Address from AddressFragment : $addres")
				if (addres.isNotEmpty()) {
					binding.edtAddressWallet.setText(addres)
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


		binding.edtAddressWallet.addTextChangedListener {
			if (it.isNullOrEmpty()) {
				twoEdtsFilled.remove(1)
			} else {
				twoEdtsFilled.add(1)
				txtEnterAddressWallet.visibility = View.VISIBLE
				edtAddressWallet.hint = ""
				line2.setBackgroundColor(curActivity().getColorResource(R.color.green))
				edtAddressWallet.setTextColor(curActivity().getColorResource(R.color.secondary_text_color))
				txtEnterAddressWallet.setTextColor(curActivity().getColorResource(R.color.green))
			}
			enableBtnContinueTwoEdtsFilled()
		}

		binding.edtEnterAmount.addTextChangedListener {
			kotlin.runCatching {
				initAmountNotEnoughWarnings()
			}.onFailure {
				VLog.d("Excepting entering amount into amount edts : ${it}")
			}
		}

		binding.edtEnterCommission.addTextChangedListener {
			kotlin.runCatching {
				initAmountNotEnoughWarnings()
			}.onFailure {

			}
		}


		binding.btnContinue.setOnClickListener {
			if (connectionLiveData.isOnline) {
				val addressWalletInValid = binding.edtAddressWallet.text.toString().isEmpty()
				if (addressWalletInValid) {
					showWarningInvalidWalledAddress()
					return@setOnClickListener
				}
				showConfirmTransactionDialog()
			} else {
				dialogManager.showNoInternetTimeOutExceptionDialog(curActivity()) {

				}
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

	private fun getSendingAmountForFee(): Double {
		if (!this@SendFragment::tokenAdapter.isInitialized)
			return 0.0
		val sendingToken = tokenAdapter.dataOptions[tokenAdapter.selectedPosition]
		if (sendingToken == "XCH" || sendingToken == "XCC")
			return binding.edtEnterAmount.text.toString().toDoubleOrNull() ?: 0.0
		return 0.0
	}

	private fun getCommissionAmount(): Double {
		val commission = binding.edtEnterCommission.text.toString().toDoubleOrNull() ?: return 0.0
		return commission
	}

	private fun isPrecisionSatisfied(amount: Double?): Boolean {
		if (amount == null || !this::tokenAdapter.isInitialized) return false
		val precision =
			getTokenPrecisionByCode(tokenAdapter.dataOptions[tokenAdapter.selectedPosition])
		return precision * amount >= 1.0
	}

	@SuppressLint("SetTextI18n")
	private fun convertAmountToUSDGAD(amount: Double) {
		lifecycleScope.launch(handler) {
			val amountInUSD =
				amount * viewModel.getTokenPriceByCode(tokenAdapter.dataOptions[tokenAdapter.selectedPosition])
			binding.txtAmountInUSD.setText("⁓${formattedDollarWithPrecision(amountInUSD, 3)}")
			val gadPrice = viewModel.getTokenPriceByCode(binding.txtGAD.text.toString())
			var convertedAmountGAD = 0.0
			if (gadPrice != 0.0) {
				convertedAmountGAD = amountInUSD / gadPrice
			}
			val tokenPrice =
				viewModel.getTokenPriceByCode(tokenAdapter.dataOptions[tokenAdapter.selectedPosition])
			binding.txtAmountInGAD.setText(
				"~${
					formattedDollarWithPrecision(
						convertedAmountGAD,
						3
					)
				}"
			)
		}
	}

	private fun checkAddressAlreadyExistInDB() {
		addressAlreadyExist?.cancel()
		addressAlreadyExist = lifecycleScope.launch {
			withContext(Dispatchers.IO) {
				val addressList =
					viewModel.checkIfAddressExistInDb(binding.edtAddressWallet.text.toString())
				VLog.d("AddressList by given address :  ${addressList.size}")
				withContext(Dispatchers.Main) {
					if (addressList.isNotEmpty()) {
						txtAddressAlredyExistWarning.visibility = View.VISIBLE
					} else {
						txtAddressAlredyExistWarning.visibility = View.GONE
					}
				}
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
				if (!this@SendFragment.isVisible) {
					edtAddressWallet.setTextColor(curActivity().getColorResource(R.color.secondary_text_color))
					txtEnterAddressWallet.setTextColor(curActivity().getColorResource(R.color.green))
					txtAddressDontExistWarning.visibility = View.GONE
				}
			}
		}
	}

	private fun initAmountNotEnoughWarnings() {
		val amountSending = binding.edtEnterAmount.text.toString().toDoubleOrNull()
		if (amountSending == null) {
			hideNotEnoughAmountWarningSending()
			hideAmountNotEnoughWarning()
			convertAmountToUSDGAD(0.0)
		}
		val amountFee = binding.edtEnterCommission.text.toString().toDoubleOrNull()
		if (amountFee == null) {
			hideNotEnoughAmountWarningFee()
			hideAmountNotEnoughWarning()
		}
		var isSendingAmountEnough = true
		if (amountSending != null) {
			convertAmountToUSDGAD(amountSending)
			val total =
				amountSending + if (tokenAdapter.selectedPosition != 0) 0.0 else (amountFee ?: 0.0)
			if (total > spendableAmountToken) {
				showNotEnoughAmountWarning()
				notEnoughAmountWarningSending()
				isSendingAmountEnough = false
			} else {
				hideAmountNotEnoughWarning()
				hideNotEnoughAmountWarningSending()
			}
		}
		if (amountFee != null) {
			val total =
				amountFee + if (tokenAdapter.selectedPosition != 0) 0.0 else (amountSending ?: 0.0)
			if (total > spendableAmountFee) {
				showNotEnoughAmountWarning()
				notEnoughAmountWarningTextFee()
			} else {
				if (isSendingAmountEnough)
					hideAmountNotEnoughWarning()
				hideNotEnoughAmountWarningFee()
			}
		}
	}

	private fun showNotEnoughAmountWarning() {
		binding.apply {
			val token = getTokenTypeThatIsNotEnough()
			val text =
				curActivity().getStringResource(R.string.send_token_insufficient_funds_error) + " $token"
			txtNotEnoughMoneyWarning.text = text
			txtNotEnoughMoneyWarning.visibility = View.VISIBLE
		}
	}

	private fun notEnoughAmountWarningSending() {
		binding.apply {
			edtEnterAmount.setTextColor(curActivity().getColorResource(R.color.red_mnemonic))
			txtEnterAmount.setTextColor(curActivity().getColorResource(R.color.red_mnemonic))
		}
	}

	private fun notEnoughAmountWarningTextFee() {
		binding.apply {
			edtEnterCommission.setTextColor(curActivity().getColorResource(R.color.red_mnemonic))
			txtEnterCommission.setTextColor(curActivity().getColorResource(R.color.red_mnemonic))
		}
	}


	private fun hideNotEnoughAmountWarningSending() {
		binding.apply {
			edtEnterAmount.setTextColor(curActivity().getColorResource(R.color.secondary_text_color))
			txtEnterAmount.setTextColor(curActivity().getColorResource(R.color.green))
		}
	}

	private fun hideNotEnoughAmountWarningFee() {
		binding.apply {
			edtEnterCommission.setTextColor(curActivity().getColorResource(R.color.secondary_text_color))
			txtEnterCommission.setTextColor(curActivity().getColorResource(R.color.green))
		}
	}

	private fun getTokenTypeThatIsNotEnough(): String {
		binding.apply {
			val enteredAmount = edtEnterAmount.text.toString().toDoubleOrNull() ?: 0.0
			var commissionAmount =
				edtEnterCommission.text.toString().toDoubleOrNull() ?: 0.0
			//check only token first
			if (tokenAdapter.selectedPosition != 0) {
				if (enteredAmount > spendableAmountToken && this@SendFragment::tokenAdapter.isInitialized)
					return tokenAdapter.dataOptions[tokenAdapter.selectedPosition]
			}
		}
		return getShortNetworkType(curNetworkType)
	}

	private fun hideAmountNotEnoughWarning() {
		binding.apply {
			edtEnterAmount.setTextColor(curActivity().getColorResource(R.color.secondary_text_color))
			txtEnterAmount.setTextColor(curActivity().getColorResource(R.color.green))
			txtNotEnoughMoneyWarning.visibility = View.GONE
		}
	}


	private fun getCommissionOfCurChosenCoin() {
		commissionJob?.cancel()
		commissionJob = lifecycleScope.launch(handler) {
			val commission = binding.edtEnterCommission.text.toString().toDoubleOrNull()
			if (commission == null)
				binding.edtEnterCommission.setText("0.00")
		}
	}

	@SuppressLint("SetTextI18n")
	private fun updateNetworkName(coinType: String) {
		VLog.d("Selected CoinType : $coinType")
		binding.apply {
			txtChosenNetwork.text = coinType
		}
	}

	private fun enableBtnContinueTwoEdtsFilled() {
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
		sendTransJob?.cancel()
		swipeJob?.cancel()
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
						curActivity().getStringResource(R.string.camera_permission_missing),
						Toast.LENGTH_SHORT
					)
						.show()
			}
		}


	override fun onDestroyView() {
		super.onDestroyView()
		curActivity().sendCoinsFragmentView = null
	}

	override fun onDestroy() {
		super.onDestroy()
	}

	private fun curActivity() = requireActivity() as MainActivity

}
