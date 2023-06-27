package com.green.wallet.presentation.main.swap.tibetswap

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.common.tools.convertNetworkTypeForFlutter
import com.example.common.tools.formatString
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.green.wallet.R
import com.green.wallet.data.network.dto.greenapp.network.NetworkItem
import com.green.wallet.databinding.DialogBtmCreateOfferBinding
import com.green.wallet.presentation.App
import com.green.wallet.presentation.custom.AnimationManager
import com.green.wallet.presentation.custom.DialogManager
import com.green.wallet.presentation.custom.formattedDollarWithPrecision
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

class BtmCreateOfferDialog : BottomSheetDialogFragment() {

	private lateinit var binding: DialogBtmCreateOfferBinding

	@Inject
	lateinit var viewModelFactory: ViewModelFactory
	private val vm: TibetSwapViewModel by viewModels { viewModelFactory }

	@Inject
	lateinit var animManager: AnimationManager

	@Inject
	lateinit var dialogManager: DialogManager


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
		binding = DialogBtmCreateOfferBinding.inflate(layoutInflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		VLog.d("On View Created Create Offer with vm : $vm")
		binding.listeners()
	}

	private fun DialogBtmCreateOfferBinding.listeners() {

		relChosenLongClick.setOnClickListener {
			clickedPositionsFee(0)
		}

		relChosenMediumClick.setOnClickListener {
			clickedPositionsFee(1)
		}

		relChosenShortClick.setOnClickListener {
			clickedPositionsFee(2)
		}

		val tokenCode = vm.tokenList.value[vm.catAdapPosition].code
		txtCAT.text = tokenCode
		txtWalletAddress.text = formatString(10, vm.curWallet?.address ?: "", 6)
		val amountFrom = vm.tibetSwap.value!!.data?.amount_in ?: 0L
		val amountTo = vm.tibetSwap.value!!.data?.amount_out ?: 0L
		val assetId = vm.tibetSwap.value?.data?.asset_id ?: ""
		val pairId = vm.tokenList.value[vm.catAdapPosition].pairID
		if (vm.tibetSwap.value?.data != null) {
			val amountIn = formattedDoubleAmountWithPrecision(
				(vm.tibetSwap.value!!.data?.amount_in
					?: 0L) / if (vm.xchToCAT) PRECISION_XCH else PRECISION_CAT
			)
			val amountOut = formattedDoubleAmountWithPrecision(
				(vm.tibetSwap.value!!.data?.amount_out
					?: 0L) / if (vm.xchToCAT) PRECISION_CAT else PRECISION_XCH
			)
			if (vm.xchToCAT) {
				txtMinus(txtXCHValue, amountIn, "XCH")
				txtPlus(txtCATValue, amountOut, tokenCode)
			} else {
				txtPlus(txtXCHValue, amountOut, "XCH")
				txtMinus(txtCATValue, amountIn, tokenCode)
			}
		}

		btnSign.setOnClickListener {
			VLog.d("Btn sign is clicked on dialog")
			if (vm.xchToCAT) {
				lifecycleScope.launch {
					generateOfferXCHToCAT(
						amountFrom = amountFrom,
						amountTo = amountTo,
						assetId = assetId
					)
				}
			} else {
				lifecycleScope.launch {
					generateOfferCATToXCH(
						amountFrom = amountFrom,
						amountTo = amountTo,
						assetId = assetId
					)
				}
			}
		}

		methodChannel.setMethodCallHandler { call, result ->
			if (call.method == "offer") {
				val resultArgs = call.arguments as HashMap<String, String>
				val offer = resultArgs["offer"].toString()
				VLog.d("Offer from Flutter : $offer")
				pushingOfferToTibet(pairId, offer)
			} else if (call.method == "exception") {
				showFailedSendingTransaction()
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
		val argSpendBundle = hashMapOf<String, Any>()
		argSpendBundle["fee"] = 0.0
		argSpendBundle["amountFrom"] = amountFrom
		argSpendBundle["amountTo"] = amountTo
		argSpendBundle["mnemonics"] = wallet.mnemonics.joinToString(" ")
		argSpendBundle["url"] = url
		argSpendBundle["asset_id"] = assetId
		argSpendBundle["observer"] = wallet.observerHash
		argSpendBundle["nonObserver"] = wallet.nonObserverHash
		VLog.d("Body From Sending Fragment to flutter : $argSpendBundle")
		methodChannel.invokeMethod("XCHToCAT", argSpendBundle)
	}

	private fun pushingOfferToTibet(pairId: String, offer: String) {
		lifecycleScope.launch {
			val res = vm.pushOfferToTibet(pairId, offer)
			when (res.state) {
				Resource.State.ERROR -> {

				}

				Resource.State.SUCCESS -> {
					showSuccessSendMoneyDialog()
				}

				Resource.State.LOADING -> {
					showFailedSendingTransaction()
				}
			}
		}
	}

	private fun showFailedSendingTransaction() {
		dialogManager.hidePrevDialogs()
		requireActivity().apply {
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


	private suspend fun generateOfferCATToXCH(
		amountFrom: Long,
		amountTo: Long,
		assetId: String
	) {
		dialogManager.showProgress(requireActivity())
		val wallet = vm.curWallet ?: return
		val url = getNetworkItemFromPrefs(wallet.networkType)!!.full_node
		val argSpendBundle = hashMapOf<String, Any>()
		argSpendBundle["fee"] = 0.0
		argSpendBundle["amountFrom"] = amountFrom
		argSpendBundle["amountTo"] = amountTo
		argSpendBundle["mnemonics"] = wallet.mnemonics.joinToString(" ")
		argSpendBundle["url"] = url
		argSpendBundle["asset_id"] = assetId
		argSpendBundle["observer"] = wallet.observerHash
		argSpendBundle["nonObserver"] = wallet.nonObserverHash
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

	private suspend fun getNetworkItemFromPrefs(networkType: String): NetworkItem? {
		val item = vm.prefsManager.getObjectString(getPreferenceKeyForNetworkItem(networkType))
		if (item.isEmpty()) return null
		return Gson().fromJson(item, NetworkItem::class.java)
	}

	private fun DialogBtmCreateOfferBinding.clickedPositionsFee(pos: Int) {
		val layouts = listOf(relChosenLong, relChosenMedium, relChosenShort)
		for (i in 0 until layouts.size) {
			if (i == pos) {
				layouts[i].visibility = View.VISIBLE
			} else {
				layouts[i].visibility = View.INVISIBLE
			}
		}
	}

	private fun changePositionsFeeCombinations(from: Int, to: Int, width: Int) {

		val key = "$from|$to"
		when (key) {
			"1|2" -> {

			}

			"1|3" -> {

			}

			"2|1" -> {

			}

			"2|3" -> {
//				animManager.moveViewToRightByWidth(binding.relChosenFee, width)
			}

			"3|1" -> {

			}

			"3|2" -> {

			}
		}

	}

	override fun getTheme(): Int {
		return R.style.AppBottomSheetDialogTheme
	}


}
