package com.green.wallet.presentation.main.dapp.trade

import androidx.lifecycle.viewModelScope
import com.green.wallet.data.preference.PrefsManager
import com.green.wallet.domain.domainmodel.Wallet
import com.green.wallet.domain.interact.BlockChainInteract
import com.green.wallet.domain.interact.SpentCoinsInteract
import com.green.wallet.domain.interact.TokenInteract
import com.green.wallet.domain.interact.WalletInteract
import com.green.wallet.presentation.main.dapp.trade.models.CatToken
import com.green.wallet.presentation.main.dapp.trade.models.NftToken
import com.green.wallet.presentation.main.dapp.trade.models.Token
import com.green.wallet.presentation.main.enterpasscode.PassCodeCommunicator
import com.green.wallet.presentation.tools.PRECISION_CAT
import com.green.wallet.presentation.tools.PRECISION_XCH
import com.green.wallet.presentation.tools.ReasonEnterCode
import com.green.wallet.presentation.tools.VLog
import com.greenwallet.core.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import kotlin.system.measureTimeMillis


class TraderViewModel @Inject constructor(
    val passCodeCommunicator: PassCodeCommunicator,
    private val tokenInteract: TokenInteract,
    private val walletInteract: WalletInteract,
    private val spentCoinsInteract: SpentCoinsInteract,
    val prefsManager: PrefsManager,
    private val blockChainInteract: BlockChainInteract
) : BaseViewModel<TraderViewState, TraderEvent>(TraderViewState()) {

    var wallet: Wallet? = null

    private val _offerViewState = MutableStateFlow(
        OfferViewState()
    )
    val offerViewState = _offerViewState.asStateFlow()

    val tempList = listOf(
        CatToken("XCC", "", 0.0013),
        CatToken("GWT", "", 0.11),
        CatToken("CHIA", "", 0.456),
        NftToken(
            "alsdjasl;dka;",
            "saldjasldak;ldal;sd",
            "https://nftstorage.link/ipfs/bafybeiee256ja5xiyrec53geiyuaz352rh2422y3zr37t3dj2xca7stixe/9766.png"
        )
    )

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
                    requested = parseTokenJson(requestedJson, true)
                )
            }
            VLog.d("OfferViewState update : ${_offerViewState.value}")
        }
    }

    fun saveSpentCoins(spentCoins: String) {
        viewModelScope.launch {
            val timeCreated = System.currentTimeMillis()
            val jsonObject = JSONObject(spentCoins)
            for (key in jsonObject.keys()) {
                val value = jsonObject[key]
                if (value == "null") {
                    spentCoinsInteract.insertSpentCoinsJson(
                        value.toString(),
                        timeCreated,
                        "XCH",
                        wallet?.address.orEmpty()
                    )
                } else {
                    val code = tokenInteract.getTokenCodeByHash(key)
                    spentCoinsInteract.insertSpentCoinsJson(
                        value.toString(), timeCreated, code, wallet?.address.orEmpty()
                    )
                }
            }
        }
    }

    private suspend fun parseTokenJson(
        json: String,
        needSpendableBalance: Boolean = false
    ): List<Token> {
        val list = mutableListOf<Token>()
        val jsonArr = JSONArray(json)
        for (i in 0 until jsonArr.length()) {
            val item = JSONObject(jsonArr.get(i).toString())

            var assetID = item.getString("assetID") ?: ""
            val assetAmount = item.getInt("assetAmount")
            val spentType = item.getString("type")
            VLog.d("SpentType : $spentType, AssetID : $assetID, AssetAmount : $assetAmount")
            var token: Token
            if (spentType == "nft") {
                val timeTaken = measureTimeMillis {
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
                VLog.d("TimeTaken to know about nft : ${timeTaken / 1000}")

            } else {
                var code = "XCH"
                if (assetID != "null") {
                    code = tokenInteract.getTokenCodeByHash(assetID)
                } else {
                    assetID = ""
                }
                val dividedAmount =
                    assetAmount / if (assetID.isEmpty()) PRECISION_XCH else PRECISION_CAT

                var spendableBalance = 0.0
                if (needSpendableBalance && false) {
                    spendableBalance = spentCoinsInteract.getSpendableBalanceByTokenCode(
                        assetID,
                        code,
                        wallet?.address ?: ""
                    ).first()
                }

                token = CatToken(
                    assetID = assetID,
                    amount = dividedAmount,
                    code = code,
                    spendableBalance = spendableBalance
                )
            }

            VLog.d("Adding Token Abstract : $token")
            list.add(token)

        }

        return list
    }

}