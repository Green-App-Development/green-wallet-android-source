package com.green.wallet.presentation.main.transaction.btmCancel

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.common.tools.convertNetworkTypeForFlutter
import com.example.common.tools.getTokenPrecisionByCode
import com.google.gson.Gson
import com.green.wallet.R
import com.green.wallet.presentation.App
import com.green.wallet.presentation.custom.DialogManager
import com.green.wallet.presentation.custom.base.BaseBottomSheetDialogFragment
import com.green.wallet.presentation.custom.convertListToStringWithSpace
import com.green.wallet.presentation.main.pincode.PinCodeFragment
import com.green.wallet.presentation.tools.METHOD_CHANNEL_GENERATE_HASH
import com.green.wallet.presentation.tools.PRECISION_XCH
import com.green.wallet.presentation.tools.ReasonEnterCode
import com.green.wallet.presentation.tools.VLog
import com.green.wallet.presentation.tools.getMainActivity
import com.green.wallet.presentation.tools.getStringResource
import com.greenwallet.core.ext.collectFlow
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

class CancelOfferDialog :
    BaseBottomSheetDialogFragment<CancelOfferViewModel>(CancelOfferViewModel::class.java) {

    private val tranID: String by lazy {
        arguments?.getString(TRANSACTION_KEY) ?: ""
    }

    @Inject
    lateinit var dialogManager: DialogManager

    val methodChannel by lazy {
        MethodChannel(
            (context?.applicationContext as App).flutterEngine.dartExecutor.binaryMessenger,
            METHOD_CHANNEL_GENERATE_HASH
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm.initOfferTransaction(tranID)
    }

    @Composable
    override fun SetUI() {
        val state by vm.viewState.collectAsStateWithLifecycle()

        CancelOfferScreen(
            state = state,
            onEvent = vm::handleEvent
        )

        LaunchedEffect(true) {
            vm.event.collectFlow(this) {
                when (it) {
                    is CancelOfferEvent.ShowPinCode -> {
                        PinCodeFragment.build(reason = ReasonEnterCode.CANCEL_OFFER)
                            .show(childFragmentManager, "")
                    }

                    is CancelOfferEvent.PinnedCodeToCancel -> {
                        sendingTransactionToItself()
                    }

                    CancelOfferEvent.ErrorCancelled -> {
                        showFailedSendingTransaction()
                    }

                    CancelOfferEvent.SuccessSending -> {
                        showSuccessSendMoneyDialog()
                    }

                    else -> Unit
                }
            }
        }
    }

    private fun showSuccessSendMoneyDialog() {
        getMainActivity().apply {
            if (!this.isDestroyed) {
                dialogManager.showSuccessDialog(
                    this,
                    getStringResource(R.string.send_token_pop_up_succsess_title),
                    getStringResource(R.string.send_token_pop_up_succsess_description),
                    getStringResource(R.string.ready_btn),
                    isDialogOutsideTouchable = false
                ) {
                    dismiss()
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

    private suspend fun sendingTransactionToItself() {
        val url = vm.getNetworkItemFromPrefs(vm.wallet.networkType)!!.full_node
        val argSpendBundle = hashMapOf<String, Any>()
        argSpendBundle["fee"] = (vm.viewState.value.fee * PRECISION_XCH).toLong()
        argSpendBundle["mnemonic"] = convertListToStringWithSpace(vm.wallet.mnemonics)
        argSpendBundle["url"] = url
        argSpendBundle["dest"] = vm.getDestinationHash()
        argSpendBundle["network_type"] = convertNetworkTypeForFlutter(vm.wallet.networkType)
        argSpendBundle["observer"] = vm.wallet.observerHash
        argSpendBundle["nonObserver"] = vm.wallet.nonObserverHash
        argSpendBundle["tranCoins"] = Gson().toJson(vm.getTranXCHCoins())
        argSpendBundle["spentCoins"] = Gson().toJson(vm.getSpentCoins())
        argSpendBundle["amount"] = 0
        methodChannel.invokeMethod("SpeedyTransferXCH", argSpendBundle)
        initListeningMethod(url)
    }

    private fun initListeningMethod(url: String) {
        methodChannel.setMethodCallHandler { method, callBack ->
            when (method.method) {
                "SpeedyTransfer" -> {
                    val spendBundleJson =
                        (method.arguments as HashMap<*, *>)["spendBundle"].toString()
                    val spentCoinsJson =
                        (method.arguments as HashMap<*, *>)["spentCoins"].toString()
                    val spentTokensJson = (method.arguments as HashMap<*, *>)["spentTokens"] ?: ""
//                    VLog.d("SpentCoins Json for sending trans : $spentCoinsJson")
                    vm.burstTransaction(
                        spendBundleJson,
                        spentCoinsJson,
                        spentTokensJson.toString(),
                        url
                    )
                }
            }
        }
    }

    override fun collectFlowOnStarted(scope: CoroutineScope) {
        vm.viewState.collectFlow(scope) {
            if (it.isLoading) {
                dialogManager.showProgress(requireActivity())
            } else {
                dialogManager.hidePrevDialogs()
            }
        }
    }

    companion object {

        private const val TRANSACTION_KEY = "transaction_key"

        fun build(tranID: String) = CancelOfferDialog().apply {
            val bundle = Bundle()
            bundle.putString(TRANSACTION_KEY, tranID)
            arguments = bundle
        }
    }


}