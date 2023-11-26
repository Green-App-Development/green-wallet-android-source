package com.green.wallet.presentation.main.dapp.trade

import androidx.lifecycle.viewModelScope
import com.green.wallet.data.preference.PrefsManager
import com.green.wallet.domain.domainmodel.Wallet
import com.green.wallet.domain.interact.TokenInteract
import com.green.wallet.domain.interact.WalletInteract
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
import javax.inject.Inject


class TraderViewModel @Inject constructor(
    val passCodeCommunicator: PassCodeCommunicator,
    private val tokenInteract: TokenInteract,
    private val walletInteract: WalletInteract,
    val prefsManager: PrefsManager,
) : BaseViewModel<TraderViewState, TraderEvent>(TraderViewState()) {

    var wallet: Wallet? = null

    private val _offerViewState = MutableStateFlow(OfferViewState())
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
        requestedAssetId: String,
        offeredAssetId: String,
        requestedAmount: Long,
        offeredAmount: Long
    ) {
        viewModelScope.launch {
            val requestedCode = tokenInteract.getTokenCodeByHash(requestedAssetId)
            val offeredCode = tokenInteract.getTokenCodeByHash(offeredAssetId)
            val dividedReqAmount =
                requestedAmount / if (requestedAssetId.isEmpty()) PRECISION_XCH else PRECISION_CAT
            val dividedOfferedAmount =
                offeredAmount / if (offeredAssetId.isEmpty()) PRECISION_XCH else PRECISION_CAT
            _offerViewState.update {
                it.copy(
                    acceptOffer = true,
                    offeringCode = offeredCode.ifEmpty { "XCH" },
                    requestingCode = requestedCode.ifEmpty { "XCH" },
                    offerAmount = dividedOfferedAmount,
                    requestingAmount = dividedReqAmount,
                    requestingAssetId = requestedAssetId
                )
            }
            VLog.d("OfferViewState update : ${_offerViewState.value}")
        }
    }

}