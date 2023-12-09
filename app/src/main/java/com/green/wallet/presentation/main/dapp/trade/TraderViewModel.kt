package com.green.wallet.presentation.main.dapp.trade

import androidx.lifecycle.viewModelScope
import com.green.wallet.data.preference.PrefsManager
import com.green.wallet.domain.domainmodel.Wallet
import com.green.wallet.domain.interact.TokenInteract
import com.green.wallet.domain.interact.WalletInteract
import com.green.wallet.presentation.main.dapp.trade.models.OfferToken
import com.green.wallet.presentation.main.enterpasscode.PassCodeCommunicator
import com.green.wallet.presentation.tools.PRECISION_CAT
import com.green.wallet.presentation.tools.PRECISION_XCH
import com.green.wallet.presentation.tools.ReasonEnterCode
import com.green.wallet.presentation.tools.VLog
import com.greenwallet.core.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject


class TraderViewModel @Inject constructor(
    val passCodeCommunicator: PassCodeCommunicator,
    private val tokenInteract: TokenInteract,
    private val walletInteract: WalletInteract,
    val prefsManager: PrefsManager,
) : BaseViewModel<TraderViewState, TraderEvent>(TraderViewState()) {

    var wallet: Wallet? = null

    private val _offerViewState = MutableStateFlow(
        OfferViewState()
    )
    val offerViewState = _offerViewState.asStateFlow()

    init {
        passCodeCommunicator.onSuccessPassCode = {
            when (it) {
                ReasonEnterCode.CONNECTION_REQUEST -> {
                    _viewState.update { it.copy(isConnected = true) }
                    JavaJSThreadCommunicator.connected = true
                    JavaJSThreadCommunicator.wait = false
                }

                else -> Unit
            }
        }

        initFirstWallet()
    }

    private fun initFirstWallet() {
        viewModelScope.launch {
            wallet = walletInteract.getHomeFirstWallet()
            VLog.d("onFirstWallet received : $wallet")
        }
    }

    fun handleEvent(event: TraderEvent) {
        setEvent(event)
    }

    fun updateOfferDialogState(offer: String) {
        _offerViewState.update { it.copy(offer = offer) }
    }

    fun updateTakingOffer(
        requestedJson: String,
        offeredJson: String
    ) {
        viewModelScope.launch {
            _offerViewState.update {
                it.copy(
                    acceptOffer = true,
                    offered = parseTokenJson(offeredJson),
                    requested = parseTokenJson(requestedJson)
                )
            }
            VLog.d("OfferViewState update : ${_offerViewState.value}")
        }
    }

    private suspend fun parseTokenJson(json: String): List<OfferToken> {
        val list = mutableListOf<OfferToken>()
        val jsonArr = JSONArray(json)
        for (i in 0 until jsonArr.length()) {
            val item = JSONObject(jsonArr.get(i).toString())

            val assetID = item.getString("assetID") ?: ""
            val assetAmount = item.getInt("assetAmount")

            var code = "XCH"
            if (assetID.isNotEmpty())
                code = tokenInteract.getTokenCodeByHash(assetID)
            val dividedAmount =
                assetAmount / if (assetID.isEmpty()) PRECISION_XCH else PRECISION_CAT

            list.add(
                OfferToken(
                    assetID = assetID,
                    assetAmount = dividedAmount,
                    code = code
                )
            )
        }

        return list
    }

}