package com.green.wallet.presentation.main.swap.tibetswap

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.common.tools.formatString
import com.example.common.tools.getPercentOfValue
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.green.wallet.R
import com.green.wallet.data.network.dto.greenapp.network.NetworkItem
import com.green.wallet.databinding.DialogBtmCreateOfferXchcatBinding
import com.green.wallet.presentation.App
import com.green.wallet.presentation.custom.AnimationManager
import com.green.wallet.presentation.custom.DialogManager
import com.green.wallet.presentation.custom.formattedDoubleAmountWithPrecision
import com.green.wallet.presentation.custom.getPreferenceKeyForNetworkItem
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.tools.METHOD_CHANNEL_GENERATE_HASH
import com.green.wallet.presentation.tools.PRECISION_CAT
import com.green.wallet.presentation.tools.PRECISION_XCH
import com.green.wallet.presentation.tools.Resource
import com.green.wallet.presentation.tools.VLog
import com.green.wallet.presentation.tools.getColorResource
import com.green.wallet.presentation.tools.getMainActivity
import com.green.wallet.presentation.tools.getStringResource
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class BtmCreateOfferXCHCATDialog : BottomSheetDialogFragment() {

	private lateinit var binding: DialogBtmCreateOfferXchcatBinding

	@Inject
	lateinit var viewModelFactory: ViewModelFactory
	private val vm: TibetSwapViewModel by viewModels { viewModelFactory }

	@Inject
	lateinit var animManager: AnimationManager

	@Inject
	lateinit var dialogManager: DialogManager

	private var feePosition = 1

	private val handler = CoroutineExceptionHandler { _, ex ->
		VLog.d("Exception caught on btn create offer dialog  : ${ex.message} ")
	}

	val methodChannel by lazy {
		MethodChannel(
			(requireActivity().application as App).flutterEngine.dartExecutor.binaryMessenger,
			METHOD_CHANNEL_GENERATE_HASH
		)
	}

	override fun onAttach(context: Context) {
		super.onAttach(context)

	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		(requireActivity().application as App).appComponent.inject(this)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = DialogBtmCreateOfferXchcatBinding.inflate(layoutInflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		VLog.d("On View Created Create Offer with vm : $vm")
		binding.listeners()
	}

	private fun initSpendableBalance(assetId: String, tokenCode: String, amountIn: Double) {
		lifecycleScope.launch(handler) {
			repeatOnLifecycle(Lifecycle.State.STARTED) {
				if (vm.xchToCAT) {
					vm.getSpendableBalanceByTokenCodeAndAddress(
						address = vm.curWallet!!.address,
						tokenCode,
						assetId
					).collectLatest { spendable ->
						val diff = spendable - (amountIn + getFeeBasedOnPosition())
						VLog.d("Spendable balance $spendable Amount : $amountIn and Fee : ${getFeeBasedOnPosition()}")
						spendableBalanceTxt(
							balance = spendable,
							isEnough = diff >= 0.0,
							tokenCode = "XCH"
						)
						vm.availableXCHAmount = spendable - amountIn
						binding.clickedPositionsFee(1)
					}
				} else {
					launch {
						vm.getSpendableBalanceByTokenCodeAndAddress(
							address = vm.curWallet!!.address,
							"XCH",
							""
						).collectLatest { spendable ->
							vm.availableXCHAmount = spendable
							binding.clickedPositionsFee(1)
						}
					}
					vm.getSpendableBalanceByTokenCodeAndAddress(
						address = vm.curWallet!!.address,
						tokenCode,
						assetId
					).collectLatest { spendable ->
						val diff = spendable - amountIn
						spendableBalanceTxt(balance = spendable, diff >= 0.0, tokenCode)
						binding.btnSign.isEnabled = diff >= 0.0
						vm.catEnough = diff >= 0.0
					}
				}
			}
		}
	}

	private fun DialogBtmCreateOfferXchcatBinding.listeners() {

		relChosenLongClick.setOnClickListener {
			clickedPositionsFee(0)
		}

		relChosenMediumClick.setOnClickListener {
			clickedPositionsFee(1)
		}

		relChosenShortClick.setOnClickListener {
			clickedPositionsFee(2)
		}

		if (vm.catAdapPosition >= vm.tokenList.value!!.size || vm.catAdapPosition == -1)
			dismiss()

		val tokenCode = vm.tokenList.value!![vm.catAdapPosition].code
		val assetID = vm.tokenList.value!![vm.catAdapPosition].hash
		txtCAT.text = tokenCode
		txtWalletAddress.text = formatString(10, vm.curWallet?.address ?: "", 6)
		var amountFrom = vm.tibetSwap.value?.data?.amount_in ?: 0L
		var amountTo = vm.tibetSwap.value?.data?.amount_out ?: 0L
		val assetId = vm.tibetSwap.value?.data?.asset_id ?: ""
		val pairId = vm.tokenList.value!![vm.catAdapPosition].pairID
		val donationAmount: Double

		val devFee =
			requireActivity().getStringResource(R.string.fee_dev).removeSuffix("%").toDoubleOrNull()
				?: 0.3
		val walletFee = requireActivity().getStringResource(R.string.fee_wallet).removeSuffix("%")
			.toDoubleOrNull() ?: 0.5

		val total = devFee + walletFee

		if (vm.xchToCAT) {
			donationAmount = getPercentOfValue(value = amountFrom, percent = total)
			amountFrom += donationAmount.toLong()
		} else {
			donationAmount = getPercentOfValue(value = amountTo, percent = total)
			amountTo -= donationAmount.toLong()
		}

		val amountIn =
			amountFrom / if (vm.xchToCAT) PRECISION_XCH else PRECISION_CAT

		val amountOut =
			amountTo / if (vm.xchToCAT) PRECISION_CAT else PRECISION_XCH

		val amountInStr = formattedDoubleAmountWithPrecision(amountIn)
		val amountOutStr = formattedDoubleAmountWithPrecision(amountOut)

		if (vm.xchToCAT) {
			txtMinus(txtXCHValue, amountInStr, "XCH")
			txtPlus(txtCATValue, amountOutStr, tokenCode)
		} else {
			txtPlus(txtXCHValue, amountOutStr, "XCH")
			txtMinus(txtCATValue, amountInStr, tokenCode)
		}

		btnSign.setOnClickListener {
			VLog.d("Btn sign is clicked on dialog")
			if (vm.xchToCAT) {
				initMethodChannelHandler(
					pairId,
					amountIn,
					amountOut,
					tokenCode,
					donationAmount,
					devFee = (devFee * 10).toInt(),
					walletFee = (walletFee * 10).toInt(),
					assetId = assetID
				)
				lifecycleScope.launch {
					generateOfferXCHToCAT(
						amountFrom = amountFrom,
						amountTo = amountTo,
						assetId = assetId
					)
				}
			} else {
				initMethodChannelHandler(
					pairId, amountIn, amountOut, tokenCode, donationAmount,
					devFee = (devFee * 10).toInt(),
					walletFee = (walletFee * 10).toInt()
				)
				lifecycleScope.launch {
					generateOfferCATToXCH(
						amountFrom = amountFrom,
						amountTo = amountTo,
						assetId = assetId,
						tokenCode = tokenCode
					)
				}
			}
		}


		initSpendableBalance(
			assetId = if (vm.xchToCAT) "" else assetID,
			tokenCode = if (vm.xchToCAT) "XCH" else tokenCode,
			amountIn = amountIn
		)
	}

	private fun initMethodChannelHandler(
		pairId: String,
		amountIn: Double,
		amountOut: Double,
		tokenCode: String,
		donationAmount: Double,
		devFee: Int = 3,
		walletFee: Int = 5,
		assetId: String = ""
	) {
		methodChannel.setMethodCallHandler { call, result ->
			if (call.method == "XCHToCAT") {
				val resultArgs = call.arguments as HashMap<String, String>
				val offer = resultArgs["offer"].toString()
				val spentXCHCoins = resultArgs["spentXCHCoins"].toString()
				VLog.d("Offer from Flutter : $offer")
				pushingOfferXCHCATToTibet(
					pairId,
					offer,
					amountIn,
					amountOut,
					tokenCode,
					vm.xchToCAT,
					getFeeBasedOnPosition(),
					spentXCHCoins = spentXCHCoins,
					donationAmount = donationAmount,
					devFee = devFee,
					walletFee = walletFee,
					assetId = assetId
				)
			} else if (call.method == "offerCATToXCH") {
				val resultArgs = call.arguments as HashMap<String, String>
				val offer = resultArgs["offer"].toString()
				val spentXCHCoins = resultArgs["XCHCoins"].toString()
				val spentCATCoins = resultArgs["CATCoins"].toString()
				VLog.d("Args from Flutter : $resultArgs")
				pushingOfferToTibetCATToXCH(
					pairId,
					offer,
					amountIn,
					amountOut,
					tokenCode,
					vm.xchToCAT,
					getFeeBasedOnPosition(),
					spentXCHCoins,
					spentCATCoins,
					donationAmount = donationAmount,
					devFee = devFee,
					walletFee = walletFee
				)
			} else if (call.method == "exception") {
				showFailedTibetSwap()
			}
		}
	}

	private suspend fun generateOfferXCHToCAT(
		amountFrom: Long,
		amountTo: Long,
		assetId: String
	) {
		dialogManager.showProgress(requireActivity())
		val wallet = vm.curWallet ?: return
		val url = getNetworkItemFromPrefs(wallet.networkType)!!.full_node
		val alreadySpentCoins =
			vm.getSpentCoinsToPushTrans(wallet.networkType, wallet.address, "XCH")
		val argSpendBundle = hashMapOf<String, Any>()
		argSpendBundle["fee"] = (getFeeBasedOnPosition() * PRECISION_XCH).toLong()
		argSpendBundle["amountFrom"] = amountFrom
		argSpendBundle["amountTo"] = amountTo
		argSpendBundle["mnemonics"] = wallet.mnemonics.joinToString(" ")
		argSpendBundle["url"] = url
		argSpendBundle["asset_id"] = assetId
		argSpendBundle["observer"] = wallet.observerHash
		argSpendBundle["nonObserver"] = wallet.nonObserverHash
		argSpendBundle["spentXCHCoins"] = Gson().toJson(alreadySpentCoins)
		VLog.d("Body From Sending Fragment to flutter : $argSpendBundle")
		methodChannel.invokeMethod("XCHToCAT", argSpendBundle)
	}


	private fun pushingOfferXCHCATToTibet(
		pairId: String,
		offer: String,
		amountFrom: Double,
		amountTo: Double,
		catCode: String,
		isInputXCH: Boolean,
		fee: Double,
		spentXCHCoins: String,
		donationAmount: Double,
		devFee: Int,
		walletFee: Int,
		assetId: String
	) {
		lifecycleScope.launch(offerXCHCATHandler) {
			vm.checkingCATHome(vm.curWallet?.address ?: "", assetId)
			val res =
				vm.pushingOfferXCHCATToTibet(
					pairId,
					offer,
					amountFrom,
					amountTo,
					catCode,
					isInputXCH,
					fee,
					spentXCHCoins,
					donationAmount = donationAmount,
					devFee,
					walletFee
				)
			when (res.state) {
				Resource.State.ERROR -> {
					showFailedTibetSwap()
				}

				Resource.State.SUCCESS -> {
					showSuccessSendMoneyDialog()
				}

				Resource.State.LOADING -> {

				}
			}
		}
	}

	private val offerXCHCATHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
		showFailedTibetSwap()
	}


	private fun pushingOfferToTibetCATToXCH(
		pairId: String,
		offer: String,
		amountFrom: Double,
		amountTo: Double,
		catCode: String,
		isInputXCH: Boolean,
		fee: Double,
		spentXCHCoins: String,
		spentCATCoins: String,
		donationAmount: Double,
		devFee: Int,
		walletFee: Int
	) {
		lifecycleScope.launch {
			val res =
				vm.pushOfferCATXCHToTibet(
					pairId,
					offer,
					amountFrom,
					amountTo,
					catCode,
					isInputXCH,
					fee,
					spentXCHCoins,
					spentCATCoins,
					donationAmount,
					devFee,
					walletFee
				)
			when (res.state) {
				Resource.State.ERROR -> {
					showFailedTibetSwap()
				}

				Resource.State.SUCCESS -> {
					showSuccessSendMoneyDialog()
				}

				Resource.State.LOADING -> {

				}
			}
		}
	}

	private fun showFailedTibetSwap() {
		dialogManager.hidePrevDialogs()
		requireActivity().apply {
			dialogManager.showWarningPriceChangeDialog(
				this,
				getStringResource(R.string.price_update),
				getStringResource(R.string.price_update_text),
				getStringResource(R.string.return_btn)
			) {

			}
		}
	}

	private fun showSuccessSendMoneyDialog() {
		vm.onSuccessTibetSwapClearingFields()
		dialogManager.hidePrevDialogs()
		getMainActivity().apply {
			if (!this.isDestroyed) {
				dialogManager.showSuccessDialog(
					this,
					getStringResource(R.string.send_token_pop_up_succsess_title),
					getStringResource(R.string.send_token_pop_up_succsess_description),
					getStringResource(R.string.ready_btn),
					isDialogOutsideTouchable = false
				) {
					Handler(Looper.myLooper()!!).postDelayed({
						popBackStackOnce()
					}, 500)
				}
			}
		}
	}

	private fun getFeeBasedOnPosition(): Double {
		return when (feePosition) {
			0 -> 0.0
			1 -> 0.00005
			else -> 0.0005
		}
	}

	private suspend fun generateOfferCATToXCH(
		amountFrom: Long,
		amountTo: Long,
		assetId: String,
		tokenCode: String
	) {
		dialogManager.showProgress(requireActivity())
		val wallet = vm.curWallet ?: return
		val url = getNetworkItemFromPrefs(wallet.networkType)!!.full_node
		val spentCoinsXCH =
			vm.getSpentCoinsToPushTrans(wallet.networkType, wallet.address, "XCH")
		val spentCoinsCAT =
			vm.getSpentCoinsToPushTrans(wallet.networkType, wallet.address, tokenCode)
		val totalCoins = spentCoinsXCH.toMutableList()
		totalCoins.addAll(spentCoinsCAT)
		val argSpendBundle = hashMapOf<String, Any>()
		argSpendBundle["fee"] = (getFeeBasedOnPosition() * PRECISION_XCH).toLong()
		argSpendBundle["amountFrom"] = amountFrom
		argSpendBundle["amountTo"] = amountTo
		argSpendBundle["mnemonics"] = wallet.mnemonics.joinToString(" ")
		argSpendBundle["url"] = url
		argSpendBundle["asset_id"] = assetId
		argSpendBundle["observer"] = wallet.observerHash
		argSpendBundle["nonObserver"] = wallet.nonObserverHash
		argSpendBundle["spentCoins"] = Gson().toJson(totalCoins)
		VLog.d("Body From Sending Fragment to flutter : $argSpendBundle")
		methodChannel.invokeMethod("CATToXCH", argSpendBundle)
	}

	private fun txtPlus(txt: TextView, value: String, token: String) {
		txt.setText("+ $value $token")
		txt.setTextColor(requireActivity().getColorResource(R.color.green))
	}

	private fun txtMinus(txt: TextView, value: String, token: String) {
		txt.setText("- $value $token")
		txt.setTextColor(requireActivity().getColorResource(R.color.red_mnemonic))
	}

	@SuppressLint("SetTextI18n")
	private fun spendableBalanceTxt(balance: Double, isEnough: Boolean, tokenCode: String) {
		val format = if (balance < 0) "0" else formattedDoubleAmountWithPrecision(balance)
		binding.txtSpendableBalanceAmount.apply {
			text =
				requireActivity().getStringResource(R.string.spendable_balance) + " $format $tokenCode"
			setTextColor(requireActivity().getColorResource(if (isEnough) R.color.greey else R.color.red_mnemonic))
		}
	}

	private suspend fun getNetworkItemFromPrefs(networkType: String): NetworkItem? {
		val item = vm.prefsManager.getObjectString(getPreferenceKeyForNetworkItem(networkType))
		if (item.isEmpty()) return null
		return Gson().fromJson(item, NetworkItem::class.java)
	}

	private fun DialogBtmCreateOfferXchcatBinding.clickedPositionsFee(pos: Int) {
		feePosition = pos
		val layouts = listOf(relChosenLong, relChosenMedium, relChosenShort)
		val txtViews = listOf(
			listOf(txtLong, textView28),
			listOf(txtMedium, textView29),
			listOf(txtShort, txtAmountFeeShort)
		)
		for (i in 0 until layouts.size) {
			if (i == pos) {
				layouts[i].visibility = View.VISIBLE
			} else {
				layouts[i].visibility = View.INVISIBLE
				requireActivity().apply {
					txtViews[i][0].setTextColor(getColorResource(R.color.greey))
					txtViews[i][1].setTextColor(getColorResource(R.color.greey))
				}
			}
		}
		val curFee = getFeeBasedOnPosition()
		if (vm.xchToCAT) {
			val enoughAmountFee = curFee <= vm.availableXCHAmount
			binding.btnSign.isEnabled = enoughAmountFee
			if (enoughAmountFee) {
				requireActivity().apply {
					txtViews[pos][0].setTextColor(getColorResource(R.color.green))
					txtViews[pos][1].setTextColor(getColorResource(R.color.white))
				}
			} else {
				requireActivity().apply {
					txtViews[pos][0].setTextColor(getColorResource(R.color.red_mnemonic))
					txtViews[pos][1].setTextColor(getColorResource(R.color.red_mnemonic))
				}
			}
		} else {
			val enoughXCH = curFee <= vm.availableXCHAmount
			binding.btnSign.isEnabled = enoughXCH && vm.catEnough
			if (enoughXCH) {
				requireActivity().apply {
					txtViews[pos][0].setTextColor(getColorResource(R.color.green))
					txtViews[pos][1].setTextColor(getColorResource(R.color.white))
				}
			} else {
				requireActivity().apply {
					txtViews[pos][0].setTextColor(getColorResource(R.color.red_mnemonic))
					txtViews[pos][1].setTextColor(getColorResource(R.color.red_mnemonic))
				}
			}
		}
	}

	override fun getTheme(): Int {
		return R.style.AppBottomSheetDialogTheme
	}


	interface OnXCHCATListener {
		fun onSuccessClearFields()
	}


}
