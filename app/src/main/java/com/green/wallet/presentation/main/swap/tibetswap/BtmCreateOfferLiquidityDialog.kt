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
import com.green.wallet.domain.domainmodel.TibetLiquidity
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

	private fun DialogBtmCreateOfferLiquidityBinding.offerValues() {

		val xchAmount = vm.xchDeposit
		val catAmount = vm.catTibetAmount
		if (vm.catLiquidityAdapterPos == -1) return
		val token = vm.tokenList.value[vm.catTibetAdapterPosition]
		val tibetToken = vm.tokenTibetList.value[vm.catLiquidityAdapterPos]
		val wallet = vm.curWallet!!
		VLog.d("Choosing token : $token  and Tibet Token : $tibetToken")
		val liquidity = vm.liquidityAmount
		if (vm.toTibet) {
			txtMinus(txtXCHValue, xchAmount, "XCH")
			txtMinus(txtCATValue, catAmount, token.code)
			txtPlus(txtLiquidityAmount, liquidity, tibetToken.code)
			//xch,cat
			initSpendableBalance(
				wallet.address,
				token.code,
				xchAmount,
				catAmount,
				catAssetId = token.hash
			)
		} else {
			txtPlus(txtXCHValue, xchAmount, "XCH")
			txtPlus(txtCATValue, catAmount, token.code)
			txtMinus(txtLiquidityAmount, liquidity, tibetToken.code)
			//tibet
			vm.availableXCHAmount = 0.0
			initSpendableBalance(wallet.address, tibetToken.code, liquidity, tibetToken.hash)
		}


		btnSign.setOnClickListener {
			val tibetLiquidity = TibetLiquidity(
				offer_id = "",
				xchAmount = xchAmount,
				catAmount = catAmount,
				catToken = token.code,
				liquidityAmount = liquidity,
				liquidityToken = tibetToken.code,
				fee = getFeeBasedOnPosition(),
				time_created = 0L,
				addLiquidity = vm.toTibet
			)
			if (vm.toTibet) {
				initMethodChannelHandler(tibetLiquidity, pairId = token.pairID)
				lifecycleScope.launch {
					vm.checkingCATHome(address = wallet.address, tibetToken.hash)
					generateOfferAddLiquidity(
						xchAmount,
						catAmount,
						liquidity,
						token.hash,
						tibetToken.hash,
						token.code
					)
				}
			} else {
				initMethodChannelHandler(tibetLiquidity, pairId = token.pairID)
				lifecycleScope.launch {
					vm.checkingCATHome(address = wallet.address, token.hash)
					generateOfferRemoveLiquidity(
						xchAmount,
						catAmount,
						liquidity,
						token.hash,
						tibetToken.hash,
						liquidityCode = tibetToken.code
					)
				}
			}
		}

	}

	private fun initSpendableBalance(
		address: String,
		liquidityCode: String,
		liquidityAmount: Double,
		liquidityAssetId: String
	) {
		lifecycleScope.launch {
			repeatOnLifecycle(Lifecycle.State.STARTED) {
				launch {
					vm.getSpendableBalanceByTokenCodeAndAddress(
						address,
						"XCH",
						""
					).collectLatest { spendable ->
						vm.availableXCHAmount = spendable
						binding.clickedPositionsFee(feePosition)
					}
				}
				vm.getSpendableBalanceByTokenCodeAndAddress(
					address,
					liquidityCode,
					liquidityAssetId
				).collectLatest { spendable ->
					vm.catEnough = spendable >= liquidityAmount
					spendableBalanceTxt(
						binding.txtSpendableBalance1,
						spendable,
						spendable >= liquidityAmount,
						liquidityCode
					)
				}
			}
		}
	}

	private fun initSpendableBalance(
		address: String,
		tokenCode: String,
		xchAmount: Double,
		catAmount: Double,
		catAssetId: String
	) {
		lifecycleScope.launch {
			repeatOnLifecycle(Lifecycle.State.STARTED) {
				launch {
					vm.getSpendableBalanceByTokenCodeAndAddress(address, "XCH", "")
						.collectLatest { spendable ->
							val total = xchAmount + getFeeBasedOnPosition()
							vm.availableXCHAmount = spendable - xchAmount
							spendableBalanceTxt(
								binding.txtSpendableBalance1,
								spendable,
								spendable >= total,
								"XCH"
							)
							binding.clickedPositionsFee(1)
						}
				}
				vm.getSpendableBalanceByTokenCodeAndAddress(address, tokenCode, catAssetId)
					.collectLatest { spendable ->
						vm.catEnough = spendable >= catAmount
						spendableBalanceTxt(
							binding.txtSpendableBalance2,
							spendable,
							spendable >= catAmount,
							tokenCode
						)
					}
			}
		}
	}


	private suspend fun generateOfferRemoveLiquidity(
		xchAmount: Double,
		catAmount: Double,
		liquidity: Double,
		tokenHash: String,
		tibetHash: String,
		liquidityCode: String
	) {
		dialogManager.showProgress(requireActivity())
		val wallet = vm.curWallet ?: return
		val url = getNetworkItemFromPrefs(wallet.networkType)!!.full_node
		val argSpendBundle = hashMapOf<String, Any>()
		val spentCoins =
			vm.getSpentCoinsToPushTrans(wallet.networkType, wallet.address, liquidityCode)
		argSpendBundle["fee"] = (getFeeBasedOnPosition() * PRECISION_XCH).toLong()
		argSpendBundle["xchAmount"] = (xchAmount * PRECISION_XCH).toLong()
		argSpendBundle["catAmount"] = (catAmount * PRECISION_CAT).toLong()
		argSpendBundle["liquidityAmount"] = (liquidity * PRECISION_CAT).toLong()
		argSpendBundle["mnemonics"] = wallet.mnemonics.joinToString(" ")
		argSpendBundle["url"] = url
		argSpendBundle["token_asset_id"] = tokenHash
		argSpendBundle["tibet_asset_id"] = tibetHash
		argSpendBundle["observer"] = wallet.observerHash
		argSpendBundle["nonObserver"] = wallet.nonObserverHash
		argSpendBundle["spentCoins"] = Gson().toJson(spentCoins)
		VLog.d("Body From Sending Fragment to flutter : $argSpendBundle")
		methodChannel.invokeMethod("CATToRemoveLiquidity", argSpendBundle)
	}

	private suspend fun generateOfferAddLiquidity(
		xchAmount: Double,
		catAmount: Double,
		liquidity: Double,
		tokenHash: String,
		tibetHash: String,
		tokenCode: String
	) {
		dialogManager.showProgress(requireActivity())
		val wallet = vm.curWallet ?: return
		val url = getNetworkItemFromPrefs(wallet.networkType)!!.full_node
		val spentCoins =
			vm.getSpentCoinsToPushTrans(wallet.networkType, wallet.address, "XCH").toMutableList()
		val catCoins = vm.getSpentCoinsToPushTrans(wallet.networkType, wallet.address, tokenCode)
		spentCoins.addAll(catCoins)
		val argSpendBundle = hashMapOf<String, Any>()
		argSpendBundle["fee"] = (getFeeBasedOnPosition() * PRECISION_XCH).toLong()
		argSpendBundle["xchAmount"] = (xchAmount * PRECISION_XCH).toLong()
		argSpendBundle["catAmount"] = (catAmount * PRECISION_CAT).toLong()
		argSpendBundle["liquidityAmount"] = (liquidity * PRECISION_CAT).toLong()
		argSpendBundle["mnemonics"] = wallet.mnemonics.joinToString(" ")
		argSpendBundle["url"] = url
		argSpendBundle["token_asset_id"] = tokenHash
		argSpendBundle["tibet_asset_id"] = tibetHash
		argSpendBundle["observer"] = wallet.observerHash
		argSpendBundle["nonObserver"] = wallet.nonObserverHash
		argSpendBundle["spentCoins"] = Gson().toJson(spentCoins)
		VLog.d("Body From Sending Fragment to flutter : $argSpendBundle")
		methodChannel.invokeMethod("CATToAddLiquidity", argSpendBundle)
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
		tibetLiquidity: TibetLiquidity,
		pairId: String
	) {
		methodChannel.setMethodCallHandler { call, result ->
			if (call.method == "CATToAddLiquidity") {
				val resultArgs = call.arguments as HashMap<*, *>
				val offer = resultArgs["offer"].toString()
				val xchCoins = resultArgs["XCHCoins"].toString()
				val catCoins = resultArgs["CATCoins"].toString()
				VLog.d("Offer from Flutter : $offer")
				addLiquidity(
					offer = offer,
					xchCoins = xchCoins,
					catCoins = catCoins,
					tibetLiquidity = tibetLiquidity,
					pairID = pairId
				)
			} else if (call.method == "CATToRemoveLiquidity") {
				val resultArgs = call.arguments as HashMap<*, *>
				val offer = resultArgs["offer"].toString()
				val spentLiquidityCoins = resultArgs["liquidityCoins"].toString()
				val spentXCHCoins = resultArgs["XCHCoins"].toString()
				removeLiquidity(
					offer = offer,
					xchCoins = spentXCHCoins,
					tibetLiquidity = tibetLiquidity,
					pairID = pairId,
					liquidityCoins = spentLiquidityCoins
				)
			}
		}
	}

	private fun removeLiquidity(
		offer: String,
		xchCoins: String,
		liquidityCoins: String,
		tibetLiquidity: TibetLiquidity,
		pairID: String
	) {
		lifecycleScope.launch {
			val res = vm.removeLiquidity(
				offer = offer,
				pairId = pairID,
				liquidCoins = liquidityCoins,
				tibetLiquid = tibetLiquidity,
				xchCoins = xchCoins
			)
			when (res.state) {
				Resource.State.ERROR -> {
					showFailedTibetSwap()
				}

				Resource.State.SUCCESS -> {
					showSuccessSendMoneyDialog()
					vm.liquidityAmount = 0.0
					vm.xchDeposit = 0.0
					vm.catTibetAmount = 0.0
				}

				Resource.State.LOADING -> {

				}
			}
		}
	}

	private fun addLiquidity(
		offer: String,
		xchCoins: String,
		catCoins: String,
		tibetLiquidity: TibetLiquidity,
		pairID: String
	) {
		lifecycleScope.launch {
			val res = vm.addLiquidity(
				offer = offer,
				pairId = pairID,
				xchCoins = xchCoins,
				catCoins = catCoins,
				tibetLiquid = tibetLiquidity
			)
			when (res.state) {
				Resource.State.ERROR -> {
					showFailedTibetSwap()
				}

				Resource.State.SUCCESS -> {
					showSuccessSendMoneyDialog()
					vm.liquidityAmount = 0.0
					vm.xchDeposit = 0.0
					vm.catTibetAmount = 0.0
				}

				Resource.State.LOADING -> {

				}
			}
		}
	}

	private val offerXCHCATHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
		showFailedTibetSwap()
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

	private fun txtPlus(txt: TextView, value: Double, token: String) {
		txt.setText("+ ${formattedDoubleAmountWithPrecision(value)} $token")
		txt.setTextColor(requireActivity().getColorResource(R.color.green))
	}

	private fun txtMinus(txt: TextView, value: Double, token: String) {
		txt.setText("- ${formattedDoubleAmountWithPrecision(value)} $token")
		txt.setTextColor(requireActivity().getColorResource(R.color.red_mnemonic))
	}

	@SuppressLint("SetTextI18n")
	private fun spendableBalanceTxt(
		txtView: TextView,
		balance: Double,
		isEnough: Boolean,
		tokenCode: String
	) {
		val format = if (balance < 0) "0" else formattedDoubleAmountWithPrecision(balance)
		txtView.apply {
			text =
				requireActivity().getStringResource(R.string.spendable_balance) + " $format $tokenCode"
			setTextColor(requireActivity().getColorResource(if (isEnough) R.color.greey else R.color.red_mnemonic))
		}
		binding.btnSign.isEnabled = isEnough
	}

	private suspend fun getNetworkItemFromPrefs(networkType: String): NetworkItem? {
		val item = vm.prefsManager.getObjectString(getPreferenceKeyForNetworkItem(networkType))
		if (item.isEmpty()) return null
		return Gson().fromJson(item, NetworkItem::class.java)
	}

	private fun DialogBtmCreateOfferLiquidityBinding.clickedPositionsFee(pos: Int) {
		feePosition = pos
		val layouts = listOf(relChosenLong, relChosenMedium, relChosenShort)
		val txtViews = listOf(
			listOf(txtLong, textView28),
			listOf(txtMedium, textView29),
			listOf(txtShort, txtShortValue)
		)
		for (i in 0 until layouts.size) {
			if (i == pos) {
				layouts[i].visibility = View.VISIBLE
			} else {
				layouts[i].visibility = View.INVISIBLE
			}
			requireActivity().apply {
				txtViews[i][0].setTextColor(getColorResource(R.color.greey))
				txtViews[i][1].setTextColor(getColorResource(R.color.greey))
			}
		}
		val curFee = getFeeBasedOnPosition()
		if (vm.toTibet) {
			val enoughXCH = curFee <= vm.availableXCHAmount
			binding.btnSign.isEnabled = enoughXCH && vm.catEnough
			if (enoughXCH) {
				requireActivity().apply {
					txtViews[pos][0].setTextColor(getColorResource(R.color.greey))
					txtViews[pos][1].setTextColor(getColorResource(R.color.greey))
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
					txtViews[pos][0].setTextColor(getColorResource(R.color.greey))
					txtViews[pos][1].setTextColor(getColorResource(R.color.greey))
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


}
