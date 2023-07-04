package com.green.wallet.presentation.main.swap.tibetswap

import android.annotation.SuppressLint
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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.green.wallet.R
import com.green.wallet.data.network.dto.greenapp.network.NetworkItem
import com.green.wallet.databinding.DialogBtmCreateOfferLiquidityBinding
import com.green.wallet.presentation.App
import com.green.wallet.presentation.custom.AnimationManager
import com.green.wallet.presentation.custom.DialogManager
import com.green.wallet.presentation.custom.formattedDoubleAmountWithPrecision
import com.green.wallet.presentation.custom.getPreferenceKeyForNetworkItem
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.tools.METHOD_CHANNEL_GENERATE_HASH
import com.green.wallet.presentation.tools.PRECISION_XCH
import com.green.wallet.presentation.tools.Resource
import com.green.wallet.presentation.tools.VLog
import com.green.wallet.presentation.tools.getColorResource
import com.green.wallet.presentation.tools.getMainActivity
import com.green.wallet.presentation.tools.getStringResource
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import javax.inject.Inject

class BtmCreateOfferLiquidityDialog : BottomSheetDialogFragment() {

	private lateinit var binding: DialogBtmCreateOfferLiquidityBinding

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

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		(requireActivity().application as App).appComponent.inject(this)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = DialogBtmCreateOfferLiquidityBinding.inflate(layoutInflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		VLog.d("On View Created Create Offer with vm : $vm")
		binding.listeners()
		binding.offerValues()
	}

	private fun initSpendableBalance(assetId: String, tokenCode: String, amountIn: Double) {
		lifecycleScope.launch(handler) {
			repeatOnLifecycle(Lifecycle.State.STARTED) {

			}
		}
	}

	private fun DialogBtmCreateOfferLiquidityBinding.offerValues() {

		val xchAmount = vm.xchDeposit
		val catAmount = vm.catTibetAmount
		val token = vm.tokenList.value[vm.catLiquidityAdapterPos]
		val tibetToken = vm.tokenTibetList.value[vm.catLiquidityAdapterPos]
		val liquidity = vm.liquidityAmount
		if (vm.toTibet) {
			txtMinus(txtXCHValue, xchAmount, "XCH")
			txtMinus(txtCATValue, catAmount, token.code)
			txtPlus(txtLiquidityAmount, liquidity, tibetToken.code)
		} else {
			txtPlus(txtXCHValue, xchAmount, "XCH")
			txtPlus(txtCATValue, catAmount, token.code)
			txtMinus(txtLiquidityAmount, liquidity, tibetToken.code)
		}

		btnSign.setOnClickListener {
			if (vm.toTibet) {

				lifecycleScope.launch {
					generateOfferToTibet(
						xchAmount,
						catAmount,
						liquidity,
						token.hash,
						tibetToken.hash
					)
				}
			} else {

			}
		}

	}

	private suspend fun generateOfferToTibet(
		xchAmount: Double,
		catAmount: Double,
		liquidity: Double,
		tokenHash: String,
		tibetHash: String
	) {
		dialogManager.showProgress(requireActivity())
		val wallet = vm.curWallet ?: return
		val url = getNetworkItemFromPrefs(wallet.networkType)!!.full_node
		val argSpendBundle = hashMapOf<String, Any>()
		argSpendBundle["fee"] = (getFeeBasedOnPosition() * PRECISION_XCH).toLong()
		argSpendBundle["xchAmount"] = xchAmount
		argSpendBundle["catAmount"] = catAmount
		argSpendBundle["liquidityAmount"] = liquidity
		argSpendBundle["mnemonics"] = wallet.mnemonics.joinToString(" ")
		argSpendBundle["url"] = url
		argSpendBundle["token_asset_id"] = tokenHash
		argSpendBundle["tibet_asset_id"] = tibetHash
		argSpendBundle["observer"] = wallet.observerHash
		argSpendBundle["nonObserver"] = wallet.nonObserverHash
		VLog.d("Body From Sending Fragment to flutter : $argSpendBundle")
		methodChannel.invokeMethod("CATToTibet", argSpendBundle)
	}

	private fun DialogBtmCreateOfferLiquidityBinding.listeners() {

		relChosenLongClick.setOnClickListener {
			clickedPositionsFee(0)
		}

		relChosenMediumClick.setOnClickListener {
			clickedPositionsFee(1)
		}

		relChosenShortClick.setOnClickListener {
			clickedPositionsFee(2)
		}

	}

	private fun initMethodChannelHandler(
		pairId: String,
		amountIn: Double,
		amountOut: Double,
		tokenCode: String
	) {
		methodChannel.setMethodCallHandler { call, result ->

		}
	}


	private fun pushingOfferXCHCATToTibet(
		pairId: String,
		offer: String,
		amountFrom: Double,
		amountTo: Double,
		catCode: String,
		isInputXCH: Boolean,
		fee: Double,
		spentXCHCoins: String
	) {
		lifecycleScope.launch(offerXCHCATHandler) {
			val res =
				vm.pushingOfferXCHCATToTibet(
					pairId,
					offer,
					amountFrom,
					amountTo,
					catCode,
					isInputXCH,
					fee,
					spentXCHCoins
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
		spentCATCoins: String
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
					spentCATCoins
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
				"Цена изменилась",
				" Верните3 сь на предыдущий шаг, чтобы получить актуальную цену",
				"Вернуться"
			) {

			}
		}
	}

	private fun showSuccessSendMoneyDialog() {
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
			1 -> 0.00000005
			else -> 0.0005
		}
	}

	private fun txtPlus(txt: TextView, value: Double, token: String) {
		txt.setText("+ ${formattedDoubleAmountWithPrecision(value)} $token")
		txt.setTextColor(requireActivity().getColorResource(R.color.green))
	}

	private fun txtMinus(txt: TextView, value: Double, token: String) {
		txt.setText("- ${formattedDoubleAmountWithPrecision(value)} $token")
		txt.setTextColor(requireActivity().getColorResource(R.color.red_mnemonic))
	}

	@SuppressLint("SetTextI18n")
	private fun spendableBalanceTxt(balance: Double, isEnough: Boolean, tokenCode: String) {
		val format = if (balance < 0) "0" else formattedDoubleAmountWithPrecision(balance)
//		binding.txtSpendableBalanceAmount.apply {
//			text =
//				requireActivity().getStringResource(R.string.spendable_balance) + " $format $tokenCode"
//			setTextColor(requireActivity().getColorResource(if (isEnough) R.color.greey else R.color.red_mnemonic))
//		}
	}

	private suspend fun getNetworkItemFromPrefs(networkType: String): NetworkItem? {
		val item = vm.prefsManager.getObjectString(getPreferenceKeyForNetworkItem(networkType))
		if (item.isEmpty()) return null
		return Gson().fromJson(item, NetworkItem::class.java)
	}

	private fun DialogBtmCreateOfferLiquidityBinding.clickedPositionsFee(pos: Int) {
		feePosition = pos
		val layouts = listOf(relChosenLong, relChosenMedium, relChosenShort)
		for (i in 0 until layouts.size) {
			if (i == pos) {
				layouts[i].visibility = View.VISIBLE
			} else {
				layouts[i].visibility = View.INVISIBLE
			}
		}
	}

	override fun getTheme(): Int {
		return R.style.AppBottomSheetDialogTheme
	}


}
