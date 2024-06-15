package com.green.wallet.presentation.main.transaction.btmSpeedy

import android.content.res.Resources.NotFoundException
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.common.tools.convertNetworkTypeForFlutter
import com.example.common.tools.getTokenPrecisionByCode
import com.google.gson.Gson
import com.green.wallet.R
import com.green.wallet.domain.domainmodel.TransferTransaction
import com.green.wallet.presentation.App
import com.green.wallet.presentation.custom.DialogManager
import com.green.wallet.presentation.custom.base.BaseBottomSheetDialogFragment
import com.green.wallet.presentation.custom.convertListToStringWithSpace
import com.green.wallet.presentation.main.dapp.trade.models.CatToken
import com.green.wallet.presentation.main.dapp.trade.models.NftToken
import com.green.wallet.presentation.main.pincode.PinCodeFragment
import com.green.wallet.presentation.tools.METHOD_CHANNEL_GENERATE_HASH
import com.green.wallet.presentation.tools.PRECISION_XCH
import com.green.wallet.presentation.tools.ReasonEnterCode
import com.green.wallet.presentation.tools.VLog
import com.green.wallet.presentation.tools.getMainActivity
import com.green.wallet.presentation.tools.getStringResource
import com.greenwallet.core.ext.collectFlow
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.launch
import javax.inject.Inject

class SpeedyBtmDialog :
    BaseBottomSheetDialogFragment<SpeedyDialogViewModel>(SpeedyDialogViewModel::class.java) {

    private val transaction: TransferTransaction? by lazy {
        arguments?.getParcelable(TRANSACTION_KEY)
    }

    @Inject
    lateinit var dialogManager: DialogManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme)
        VLog.d("SpeedyBtmDialog : on Create got called  : $this")
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
                        PinCodeFragment.build(reason = ReasonEnterCode.SPEEDY_TRAN)
                            .show(childFragmentManager, "")
                    }

                    SpeedyTokenEvent.OnSpeedError -> {
                        showFailedSendingTransaction()
                    }

                    SpeedyTokenEvent.OnSpeedSuccess -> {
                        showSuccessSendMoneyDialog()
                    }

                    SpeedyTokenEvent.ConfirmedPinCode -> {
                        when (state.token) {
                            is NftToken -> {
                                nftTokenRePush()
                            }

                            is CatToken -> {
                                catTokenRePush()
                            }
                        }
                        vm.setLoading(true)
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        initViewState()
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private fun initViewState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.viewState.collectFlow(this@launch) {
                    initLoadingState(it)
                }
            }
        }
    }

    private fun initLoadingState(it: SpeedyTokenState) {
        if (it.isLoading) {
            dialogManager.showProgress(requireActivity())
        } else {
            dialogManager.hidePrevDialogs()
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
                    dismiss()
                }
            }
        }
    }

    private suspend fun nftTokenRePush() {
        val url = vm.getNetworkItemFromPrefs(vm.wallet.networkType)!!.full_node
        val args = hashMapOf<String, Any>()
        args["observer"] = vm.wallet.observerHash
        args["non_observer"] = vm.wallet.nonObserverHash
        args["destAddress"] =
            transaction?.toDestHash ?: throw NotFoundException("Not found nft tran dest hash")
        args["mnemonics"] = convertListToStringWithSpace(vm.wallet.mnemonics)
        args["coinParentInfo"] = vm.nftCoin.coinInfo
        args["base_url"] = url
        args["spentCoins"] = Gson().toJson(vm.getSpentCoins())
        args["tranCoins"] = Gson().toJson(vm.getTranCoins())
        args["fromAddress"] = vm.nftCoin.puzzleHash
        args["fee"] = (vm.viewState.value.fee * PRECISION_XCH).toLong()
        methodChannel.invokeMethod("SpeedyTransferNFT", args)
        initListeningMethod(url)
    }

    private suspend fun catTokenRePush() {
        val catToken = vm.viewState.value.token as CatToken
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

                "SpeedyTransferNFT" -> {
                    VLog.d("SpendBundleJson for NFT from flutter : ${method.arguments}")
                    val argAnd = (method.arguments as HashMap<*, *>)
                    val spendBundleJson =
                        argAnd["spendBundle"].toString()
                    val spentCoinsJson =
                        argAnd["spentCoins"].toString()
                    vm.burstTransactionNFT(spendBundleJson, spentCoinsJson, url)
                }
            }
        }
    }

    companion object {

        private const val TRANSACTION_KEY = "transaction_key"

        fun build(transaction: TransferTransaction?) = SpeedyBtmDialog().apply {
            val bundle = Bundle()
            bundle.putParcelable(TRANSACTION_KEY, transaction)
            arguments = bundle
        }
    }

}