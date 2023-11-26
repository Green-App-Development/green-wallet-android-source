package com.green.wallet.presentation.main.dapp.trade

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.viewModels
import com.google.gson.Gson
import com.green.compose.theme.GreenWalletTheme
import com.green.wallet.data.network.dto.greenapp.network.NetworkItem
import com.green.wallet.presentation.App
import com.green.wallet.presentation.custom.base.BaseComposeFragment
import com.green.wallet.presentation.custom.getPreferenceKeyForNetworkItem
import com.green.wallet.presentation.di.factory.ViewModelFactory
import com.green.wallet.presentation.tools.METHOD_CHANNEL_GENERATE_HASH
import com.green.wallet.presentation.tools.ReasonEnterCode
import com.green.wallet.presentation.tools.VLog
import com.green.wallet.presentation.tools.getMainActivity
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

class TraderFragment : BaseComposeFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: TraderViewModel by viewModels { viewModelFactory }

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
                viewModel.event.collect {
                    VLog.d("TraderEvent received : $it")
                    when (it) {
                        TraderEvent.ShowPinCode -> {
                            getMainActivity().showEnterPasswordFragment(
                                reason = ReasonEnterCode.CONNECTION_REQUEST
                            )
                        }

                        is TraderEvent.ParseTakeOffer -> {
                            callFlutterTakeOffer(it.offer)
                            viewModel.updateOfferDialogState(it.offer)
                            viewModel.handleEvent(TraderEvent.ShowTakeOfferDialog)
                        }

                        is TraderEvent.ShowCreateOfferDialog -> {

                        }

                        is TraderEvent.SendOffer -> {
                            callFlutterToPushTakeOffer(viewModel.offerViewState.value.offer)
                        }

                        else -> Unit
                    }
                }
            }
        }

        initListeningMethod()
    }

    private fun initListeningMethod() {
        methodChannel.setMethodCallHandler { call, callBack ->
            when (call.method) {
                "AnalyzeOffer" -> {
                    val arguments = (call.arguments as HashMap<*, *>)
                    val requestedAssetId = arguments["requested_asset_id"]?.toString().orEmpty()
                    val offeredAssetId = arguments["offered_asset_id"]?.toString().orEmpty()
                    val requestedAmount =
                        Math.abs(arguments["requested_amount"]?.toString()?.toLong() ?: 0L)
                    val offeredAmount =
                        Math.abs(arguments["offered_amount"]?.toString()?.toLong() ?: 0L)
                    viewModel.updateTakingOffer(
                        requestedAssetId,
                        offeredAssetId,
                        requestedAmount,
                        offeredAmount
                    )
                }
            }
        }
    }

    fun callFlutterTakeOffer(offer: String) {
        val map = hashMapOf<String, Any>()
        map["offer"] = offer
        VLog.d("Invoked method to parse offer : $map")
        methodChannel.invokeMethod("AnalyzeOffer", map)
    }

    suspend fun callFlutterToPushTakeOffer(offer: String) {
        val map = hashMapOf<String, Any>()
        map["offer"] = offer
        val wallet = viewModel.wallet ?: return
        val url = getNetworkItemFromPrefs(wallet.networkType)!!.full_node
        map["observer"] = wallet.observerHash
        map["nonObserver"] = wallet.nonObserverHash
        map["url"] = url
        map["fee"] = 0
        map["mnemonics"] = wallet.mnemonics.joinToString(" ")
        map["asset_id"] = viewModel.offerViewState.value.requestingAssetId
        methodChannel.invokeMethod("PushingOffer", map)
    }

    private suspend fun getNetworkItemFromPrefs(networkType: String): NetworkItem? {
        val item =
            viewModel.prefsManager.getObjectString(getPreferenceKeyForNetworkItem(networkType))
        if (item.isEmpty()) return null
        return Gson().fromJson(item, NetworkItem::class.java)
    }

    override fun collectFlowOnStart(scope: CoroutineScope) {

    }


}