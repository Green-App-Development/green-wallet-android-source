package com.green.wallet.presentation.main.dapp.trade

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Surface
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.green.compose.theme.GreenWalletTheme
import com.green.compose.theme.Provider
import com.green.compose.web_header.WebViewHeader
import com.green.wallet.presentation.main.dapp.trade.bottom.ModelBottomSheetConnect
import com.green.wallet.presentation.main.dapp.trade.bottom.ModelBottomSheetOffer
import com.green.wallet.presentation.main.dapp.trade.components.DropDownWebViewHeader
import com.green.wallet.presentation.tools.VLog
import de.andycandy.android.bridge.Bridge
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TraderScreen(
    state: TraderViewState = TraderViewState(),
    offerViewState: OfferViewState = OfferViewState(),
    webView: (WebView) -> Unit = {},
    onEvent: (TraderEvent) -> Unit = {},
    events: Flow<TraderEvent> = flowOf()
) {

    val context = LocalContext.current

    val connectionSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden
    )

    val offerDialog = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )

    var dropDownMenu by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(connectionSheetState.currentValue) {
        if (connectionSheetState.currentValue == ModalBottomSheetValue.Hidden) {
            if (!state.isConnected) {
                VLog.d("BottomSheet hidden, notifying js side")
                JavaJSThreadCommunicator.connected = false
                JavaJSThreadCommunicator.wait = false
            }
        }
    }

    LaunchedEffect(true) {
        events.collectLatest {
            when (it) {
                TraderEvent.ShowConnectionDialog -> {
                    connectionSheetState.show()
                }

                TraderEvent.ShowTakeOfferDialog -> {
                    offerDialog.show()
                }

                else -> Unit
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                WebViewHeader(
                    modifier = Modifier.background(
                        color = Provider.current.background
                    ),
                    threeDots = {
                        dropDownMenu = true
                    },
                    back = {

                    },
                    closeX = {

                    }
                )

                DropDownWebViewHeader(
                    expanded = dropDownMenu,
                    close = {
                        dropDownMenu = false
                    }
                )
            }

            WebViewContainer(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.Black),
                url = "",
                mContext = context,
                webView = webView,
                onEvent = onEvent,
                state = state
            )
        }
        ModelBottomSheetConnect(
            isConnected = state.isConnected,
            sheetState = connectionSheetState,
        ) {
            if (!state.isConnected)
                onEvent(TraderEvent.ShowPinCode)
            else {
                scope.launch {
                    connectionSheetState.hide()
                }
            }
        }

        ModelBottomSheetOffer(
            sheetState = offerDialog,
            state = offerViewState,
            modifier = Modifier,
            sign = {
                onEvent(TraderEvent.SignOffer(offerViewState.offer, 311))
            }
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewContainer(
    modifier: Modifier = Modifier,
    url: String,
    mContext: Context,
    webView: (WebView) -> Unit,
    onEvent: (TraderEvent) -> Unit,
    state: TraderViewState
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webViewClient = WebViewClient()
                webView(this)
                val bridge = Bridge(mContext, this)
                bridge.addJSInterface(
                    GreenWalletJS(
                        onEvent = onEvent
                    )
                )
//                this.loadUrl("https://green-app-sigma.vercel.app/")
                this.loadUrl("file:///android_asset/index.html")
                this.webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        bridge.init()
                    }
                }
            }
        },
        modifier = modifier,
        update = {

        }
    )
}

@Preview
@Composable
fun TraderScreenPreview() {
    GreenWalletTheme {
        TraderScreen(
            webView = {},
            onEvent = {}
        )
    }
}