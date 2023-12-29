package com.green.wallet.presentation.main.dapp.trade

import androidx.lifecycle.viewModelScope
import com.green.wallet.data.preference.PrefsManager
import com.green.wallet.domain.domainmodel.Wallet
import com.green.wallet.domain.interact.BlockChainInteract
import com.green.wallet.domain.interact.TokenInteract
import com.green.wallet.domain.interact.WalletInteract
import com.green.wallet.presentation.main.dapp.trade.models.CatToken
import com.green.wallet.presentation.main.dapp.trade.models.NftToken
import com.green.wallet.presentation.main.dapp.trade.models.TokenOffer
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
    private val blockChainInteract: BlockChainInteract
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

    private suspend fun parseTokenJson(json: String): List<TokenOffer> {
        val list = mutableListOf<TokenOffer>()
        val jsonArr = JSONArray(json)
        for (i in 0 until jsonArr.length()) {
            val item = JSONObject(jsonArr.get(i).toString())

            val assetID = item.getString("assetID") ?: ""
            val assetAmount = item.getInt("assetAmount")
            val spentType = item.getString("type")
            VLog.d("SpentType : $spentType, AssetID : $assetID, AssetAmount : $assetAmount")
            var token: TokenOffer? = null
            when (spentType) {

                "null" -> {
                    var code = "XCH"
                    if (assetID != "null") {
                        code = tokenInteract.getTokenCodeByHash(assetID)
                    }
                    val dividedAmount =
                        assetAmount / if (assetID.isEmpty()) PRECISION_XCH else PRECISION_CAT

                    token = CatToken(
                        assetID = assetID,
                        amount = dividedAmount,
                        code = code
                    )

                }

                "nft" -> {
                    val nftInfo =
                        blockChainInteract.getNftInfoByCoinID(
                            "Chia Network",
                            coinID = assetID
                        )
                    token = NftToken(
                        collection = nftInfo?.collection ?: "",
                        nftId = nftInfo?.nftID ?: "",
                        imgUrl = nftInfo?.imageUrl ?: ""
                    )
                }
            }

            if (token != null) {
                VLog.d("Adding Token Abstract : $token")
                list.add(token)
            }
        }

        return list
    }

}