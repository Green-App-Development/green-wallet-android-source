package com.green.wallet.presentation.main.transaction.btmCancel

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.common.tools.convertNetworkTypeForFlutter
import com.example.common.tools.getTokenPrecisionByCode
import com.google.gson.Gson
import com.green.wallet.presentation.App
import com.green.wallet.presentation.custom.base.BaseBottomSheetDialogFragment
import com.green.wallet.presentation.custom.convertListToStringWithSpace
import com.green.wallet.presentation.tools.METHOD_CHANNEL_GENERATE_HASH
import com.green.wallet.presentation.tools.PRECISION_XCH
import com.green.wallet.presentation.tools.VLog
import com.greenwallet.core.ext.collectFlow
import io.flutter.plugin.common.MethodChannel

class CancelOfferDialog :
    BaseBottomSheetDialogFragment<CancelOfferViewModel>(CancelOfferViewModel::class.java) {

    private val tranID: String by lazy {
        arguments?.getString(TRANSACTION_KEY) ?: ""
    }

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
                    is CancelOfferEvent.OnSign -> {
                        //need to call pin code
                        VLog.d("On Sign cancelling offering")
                        sendingTransactionToItself()
                    }

                    else -> Unit
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

    companion object {

        private const val TRANSACTION_KEY = "transaction_key"

        fun build(tranID: String) = CancelOfferDialog().apply {
            val bundle = Bundle()
            bundle.putString(TRANSACTION_KEY, tranID)
            arguments = bundle
        }
    }


}