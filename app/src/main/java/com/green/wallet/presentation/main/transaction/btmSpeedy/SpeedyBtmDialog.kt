package com.green.wallet.presentation.main.transaction.btmSpeedy

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.common.tools.convertNetworkTypeForFlutter
import com.example.common.tools.getTokenPrecisionByCode
import com.google.gson.Gson
import com.green.wallet.R
import com.green.wallet.domain.domainmodel.Transaction
import com.green.wallet.presentation.App
import com.green.wallet.presentation.custom.base.BaseBottomSheetDialogFragment
import com.green.wallet.presentation.custom.convertListToStringWithSpace
import com.green.wallet.presentation.main.dapp.trade.models.CatToken
import com.green.wallet.presentation.main.dapp.trade.models.NftToken
import com.green.wallet.presentation.tools.METHOD_CHANNEL_GENERATE_HASH
import com.green.wallet.presentation.tools.PRECISION_XCH
import com.green.wallet.presentation.tools.VLog
import com.greenwallet.core.ext.collectFlow
import io.flutter.plugin.common.MethodChannel
import java.util.HashMap

class SpeedyBtmDialog :
    BaseBottomSheetDialogFragment<SpeedyDialogViewModel>(SpeedyDialogViewModel::class.java) {

    private val transaction: Transaction? by lazy {
        arguments?.getParcelable(TRANSACTION_KEY)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme)
    }

    val methodChannel by lazy {
        MethodChannel(
            (context?.applicationContext as App).flutterEngine.dartExecutor.binaryMessenger,
            METHOD_CHANNEL_GENERATE_HASH
        )
    }

    @Composable
    override fun SetUI() {

        vm.setCurTransaction(transaction)
        val state by vm.viewState.collectAsStateWithLifecycle()

        SpeedyTransactionBtmScreen(
            state = state,
            onEvent = vm::handleEvent
        )

        LaunchedEffect(true) {
            vm.event.collectFlow(this) {
                when (it) {
                    SpeedyTokenEvent.OnSign -> {
                        when (state.token) {
                            is NftToken -> {
                                nftTokenRePush(state.token as NftToken)
                            }

                            is CatToken -> {
                                catTokenRePush(state.token as CatToken)
                            }
                        }
                    }

                    else -> Unit
                }
            }
        }
    }

    private suspend fun nftTokenRePush(nftToken: NftToken) {


    }

    private suspend fun catTokenRePush(catToken: CatToken) {
        val url = vm.getNetworkItemFromPrefs(vm.wallet.networkType)!!.full_node
        val argSpendBundle = hashMapOf<String, Any>()
        argSpendBundle["fee"] = (vm.viewState.value.fee * PRECISION_XCH).toLong()
        argSpendBundle["amount"] =
            (catToken.amount * getTokenPrecisionByCode(catToken.code)).toLong()
        argSpendBundle["mnemonic"] = convertListToStringWithSpace(vm.wallet.mnemonics)
        argSpendBundle["url"] = url
        argSpendBundle["dest"] = transaction?.toDestHash ?: ""
        argSpendBundle["network_type"] = convertNetworkTypeForFlutter(vm.wallet.networkType)
        argSpendBundle["observer"] = vm.wallet.observerHash
        argSpendBundle["nonObserver"] = vm.wallet.nonObserverHash
        argSpendBundle["assetID"] = catToken.assetID
        argSpendBundle["tranCoins"] = Gson().toJson(vm.getTranCoins())
        argSpendBundle["spentCoins"] = Gson().toJson(vm.getSpentCoins())
        if (catToken.code == "XCH" || catToken.code == "XCC") {
            VLog.d("Called method SpeedyTransferXCH with args : $argSpendBundle")
            methodChannel.invokeMethod("SpeedyTransferXCH", argSpendBundle)
        } else {
            VLog.d("Called method SpeedyTransferCAT with args : $argSpendBundle")
            methodChannel.invokeMethod("SpeedyTransferCAT", argSpendBundle)
        }
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
                    VLog.d("SpentCoins Json for sending trans : $spentCoinsJson")
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

        fun build(transaction: Transaction?) = SpeedyBtmDialog().apply {
            val bundle = Bundle()
            bundle.putParcelable(TRANSACTION_KEY, transaction)
            arguments = bundle
        }
    }

}