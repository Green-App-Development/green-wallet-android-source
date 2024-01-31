package com.green.wallet.presentation.main.dapp.trade

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.viewModels
import com.google.gson.Gson
import com.green.compose.theme.GreenWalletTheme
import com.green.wallet.R
import com.green.wallet.data.network.dto.greenapp.network.NetworkItem
import com.green.wallet.presentation.App
import com.green.wallet.presentation.custom.DialogManager
import com.green.wallet.presentation.custom.base.BaseComposeFragment
import com.green.wallet.presentation.custom.getPreferenceKeyForNetworkItem
import com.green.wallet.presentation.custom.isThisChivesNetwork
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.main.dapp.trade.models.CatToken
import com.green.wallet.presentation.main.dapp.trade.models.FlutterToken
import com.green.wallet.presentation.main.dapp.trade.models.NftToken
import com.green.wallet.presentation.main.dapp.trade.models.Token
import com.green.wallet.presentation.main.dapp.trade.params.CreateOfferParams
import com.green.wallet.presentation.main.pincode.PinCodeFragment
import com.green.wallet.presentation.tools.METHOD_CHANNEL_GENERATE_HASH
import com.green.wallet.presentation.tools.PRECISION_CAT
import com.green.wallet.presentation.tools.PRECISION_XCH
import com.green.wallet.presentation.tools.ReasonEnterCode
import com.green.wallet.presentation.tools.VLog
import com.green.wallet.presentation.tools.getMainActivity
import com.green.wallet.presentation.tools.getStringResource
import com.greenwallet.core.ext.collectFlow
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class TraderFragment : BaseComposeFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: TraderViewModel by viewModels { viewModelFactory }

    @Inject
    lateinit var dialogManager: DialogManager

    val methodChannel by lazy {
        MethodChannel(
            (context?.applicationContext as App).flutterEngine.dartExecutor.binaryMessenger,
            METHOD_CHANNEL_GENERATE_HASH
        )
    }

    @Composable
    override fun SetUI() {

        val state by viewModel.viewState.collectAsState()

        val offerState by viewModel.offerViewState.collectAsState()

        GreenWalletTheme {
            TraderScreen(
                state = state,
                offerViewState = offerState,
                webView = {},
                onEvent = viewModel::handleEvent,
                events = viewModel.event
            )

            LaunchedEffect(true) {
                launch {
                    viewModel.event.collect {
                        VLog.d("TraderEvent received : $it")
                        when (it) {
                            TraderEvent.ShowPinCode -> {
                                PinCodeFragment.build(reason = ReasonEnterCode.CONNECTION_REQUEST)
                                    .show(childFragmentManager, "")
                            }

                            is TraderEvent.ParseTakeOffer -> {
                                viewModel.updateOfferDialogState(it.offer)
                                callFlutterTakeOffer(it.offer)
                                viewModel.handleEvent(TraderEvent.ShowTakeOfferDialog)
                            }

                            is TraderEvent.TakeOffer -> {
                                PinCodeFragment.build(reason = ReasonEnterCode.ACCEPT_OFFER)
                                    .show(childFragmentManager, "")

                            }

                            is TraderEvent.PinConfirmAcceptOffer -> {
                                viewModel.setLoading(true)
                                val value = viewModel.offerViewState.value
                                callFlutterToPushTakeOffer(
                                    value.offer,
                                    value.chosenFee
                                )
                            }

                            is TraderEvent.FailureTakingOffer -> {
                                showFailedSendingTransaction()
                            }

                            is TraderEvent.SuccessTakingOffer -> {
                                showSuccessSendMoneyDialog()
                            }

                            is TraderEvent.ShowPinCreateOffer -> {
                                PinCodeFragment.build(reason = ReasonEnterCode.CREATE_OFFER)
                                    .show(childFragmentManager, "")
                            }

                            is TraderEvent.PinnedCreateOffer -> {
                                callFlutterToCreateOffer()
                            }

                            else -> Unit
                        }
                    }
                }
            }
        }

    }

    private fun initListeningMethod() {
        methodChannel.setMethodCallHandler { call, callBack ->
            when (call.method) {
                "AnalyzeOffer" -> {
                    val arguments = (call.arguments as HashMap<*, *>)
                    VLog.d("Analyzing offer from flutter ${call.arguments}")
                    val requested = arguments["requested"]?.toString().orEmpty()
                    val offered = arguments["offered"]?.toString().orEmpty()
                    viewModel.updateTakingOffer(
                        requestedJson = requested,
                        offeredJson = offered
                    )
                }

                "CreateOffer" -> {
                    val args = (call.arguments as HashMap<*, *>)

                }

                "PushingOffer" -> {
                    val arguments = (call.arguments as HashMap<*, *>)
                    VLog.d("PushingOffer result from flutter ${call.arguments}")
                    val spentCoins = arguments["spentCoins"].toString()
                    viewModel.saveSpentCoins(spentCoins)
                }

                "ErrorPushingOffer" -> {
                    JavaJSThreadCommunicator.resultTakeOffer = ""
                    JavaJSThreadCommunicator.wait = false
                    viewModel.handleEvent(TraderEvent.FailureTakingOffer)
                }
            }
        }
    }

    private fun callFlutterTakeOffer(offer: String) {
        val map = hashMapOf<String, Any>()
        map["offer"] = offer
        VLog.d("Invoked method to parse offer : $map")
        methodChannel.invokeMethod("AnalyzeOffer", map)
        initListeningMethod()
    }

    private suspend fun callFlutterToPushTakeOffer(offer: String, fee: Double) {
        val map = hashMapOf<String, Any>()
        map["offer"] = offer
        val wallet = viewModel.wallet ?: return
        val url = getNetworkItemFromPrefs(wallet.networkType)!!.full_node
        map["observer"] = wallet.observerHash
        map["nonObserver"] = wallet.nonObserverHash
        map["url"] = url
        map["fee"] = Math.round(
            fee * if (isThisChivesNetwork(wallet.networkType)) Math.pow(
                10.0,
                8.0
            )
            else Math.pow(
                10.0,
                12.0
            )
        )
        map["mnemonics"] = wallet.mnemonics.joinToString(" ")
        map["spentCoins"] = ""
        methodChannel.invokeMethod("PushingOffer", map)
        initListeningMethod()
    }

    private suspend fun callFlutterToCreateOffer() {
        val map = hashMapOf<String, Any>()
        val wallet = viewModel.wallet ?: return
        val value = viewModel.offerViewState.value
        val url = getNetworkItemFromPrefs(wallet.networkType)!!.full_node
        map["observer"] = wallet.observerHash
        map["nonObserver"] = wallet.nonObserverHash
        map["url"] = url
        map["fee"] = (value.chosenFee * PRECISION_XCH).toLong()
        map["spentCoins"] = value.spendCoins
        map["mnemonics"] = wallet.mnemonics.joinToString(" ")
        map["request"] = Gson().toJson(convertToTokenFlutter(value.requested))
        map["offer"] = Gson().toJson(convertToTokenFlutter(value.offered))
        methodChannel.invokeMethod("CreateOffer", map)
        initListeningMethod()
    }

    private suspend fun getNetworkItemFromPrefs(networkType: String): NetworkItem? {
        val item =
            viewModel.prefsManager.getObjectString(getPreferenceKeyForNetworkItem(networkType))
        if (item.isEmpty()) return null
        return Gson().fromJson(item, NetworkItem::class.java)
    }

    override fun collectFlowOnStart(scope: CoroutineScope) {
        viewModel.viewState.collectFlow(scope) {
            if (it.isLoading) {
                dialogManager.showProgress(requireActivity())
            } else {
                dialogManager.hidePrevDialogs()
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

                }
            }
        }

    }

    private fun showFailedSendingTransaction() {
        dialogManager.hidePrevDialogs()
        getMainActivity().apply {
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

    private fun convertToTokenFlutter(list: List<Token>): List<FlutterToken> {
        val converted = mutableListOf<FlutterToken>()

        for (i in list) {
            val token = when (i) {
                is NftToken -> {
                    FlutterToken("", 0L, "NFT")
                }

                is CatToken -> {
                    val amount = i.amount * if (i.assetID.isEmpty()) {
                        PRECISION_XCH
                    } else {
                        PRECISION_CAT
                    }
                    FlutterToken(i.assetID, amount.toLong(), "CAT")
                }

                else -> FlutterToken("", 0L, "CAT")
            }
            converted.add(token)
        }

        return converted
    }


}