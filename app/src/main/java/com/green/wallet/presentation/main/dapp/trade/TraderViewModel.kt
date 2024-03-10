package com.green.wallet.presentation.main.dapp.trade

import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.green.wallet.data.network.dto.greenapp.network.NetworkItem
import com.green.wallet.data.preference.PrefsManager
import com.green.wallet.domain.domainmodel.NFTCoin
import com.green.wallet.domain.domainmodel.OfferTransaction
import com.green.wallet.domain.domainmodel.SpentCoin
import com.green.wallet.domain.domainmodel.Wallet
import com.green.wallet.domain.interact.BlockChainInteract
import com.green.wallet.domain.interact.DexieInteract
import com.green.wallet.domain.interact.NFTInteract
import com.green.wallet.domain.interact.OfferTransactionInteract
import com.green.wallet.domain.interact.SpentCoinsInteract
import com.green.wallet.domain.interact.TokenInteract
import com.green.wallet.domain.interact.WalletInteract
import com.green.wallet.presentation.custom.getPreferenceKeyForNetworkItem
import com.green.wallet.presentation.di.appState.ConnectedDApp
import com.green.wallet.presentation.main.dapp.trade.models.CatToken
import com.green.wallet.presentation.main.dapp.trade.models.NftToken
import com.green.wallet.presentation.main.dapp.trade.models.Token
import com.green.wallet.presentation.main.dapp.trade.params.AssetAmount
import com.green.wallet.presentation.main.dapp.trade.params.CreateOfferParams
import com.green.wallet.presentation.main.enterpasscode.PassCodeCommunicator
import com.green.wallet.presentation.main.pincode.PinCodeCommunicator
import com.green.wallet.presentation.tools.PRECISION_CAT
import com.green.wallet.presentation.tools.PRECISION_XCH
import com.green.wallet.presentation.tools.ReasonEnterCode
import com.green.wallet.presentation.tools.VLog
import com.greenwallet.core.base.BaseIntentViewModel
import com.greenwallet.core.base.BaseViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import kotlin.math.abs


class TraderViewModel @Inject constructor(
    passCodeCommunicator: PassCodeCommunicator,
    private val tokenInteract: TokenInteract,
    private val walletInteract: WalletInteract,
    private val spentCoinsInteract: SpentCoinsInteract,
    val prefsManager: PrefsManager,
    private val blockChainInteract: BlockChainInteract,
    private val dexieInteract: DexieInteract,
    private val nftInteractor: NFTInteract,
    private val pinCodeCommunicator: PinCodeCommunicator,
    private val offerTransactionInteract: OfferTransactionInteract,
    private val connectedDApp: ConnectedDApp
) : BaseIntentViewModel<TraderViewState, TraderEvent, TraderIntent>(TraderViewState()) {

    var wallet: Wallet? = null

    private var requestedXCHAmount: Double = 0.0

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

        pinCodeCommunicator.onSuccessPassCode = {
            when (it) {
                ReasonEnterCode.CONNECTION_REQUEST -> {
                    connectedDApp.connected.add("Dexie")
                    _viewState.update { it.copy(isConnected = true) }
                    JavaJSThreadCommunicator.connected = true
                    JavaJSThreadCommunicator.wait = false
                }

                ReasonEnterCode.ACCEPT_OFFER -> {
                    setEvent(TraderEvent.PinConfirmAcceptOffer)
                }

                ReasonEnterCode.CREATE_OFFER -> {
                    setEvent(TraderEvent.PinnedCreateOffer)
                }

                else -> Unit
            }
        }

        initFirstWallet()

        checkConnectedDApp()
    }

    private fun checkConnectedDApp() {
        _viewState.update { it.copy(isConnected = connectedDApp.connected.contains("Dexie")) }
    }

    private fun initFirstWallet() {
        viewModelScope.launch {
            wallet = walletInteract.getHomeFirstWallet()
            VLog.d("onFirstWallet received Spendable Balance for trader : ${wallet?.balance}")
            VLog.d("onFirstWallet trader: $wallet")
            validateFeeEnough()
            initDexieFee()
//            offerTransactionInteract.saveOfferTransaction(
//                wallet!!.address,
//                OfferTransaction(
//                    transId = "tran1",
//                    System.currentTimeMillis(),
//                    acceptOffer = true,
//                    fee = 0.0003,
//                    source = "dexie.space",
//                    height = 31131
//                )
//            )
        }
    }

    private suspend fun initDexieFee() {
        val dexieFee = dexieInteract.getDexieMinFee().recommended
        _offerViewState.update { it.copy(dexieFee = dexieFee) }
    }

    override fun handleIntent(intent: TraderIntent) {
        when (intent) {

            else -> Unit
        }
    }

    fun handleEvent(event: TraderEvent) {
        when (event) {
            is TraderEvent.ChoseFee -> {
                _offerViewState.update {
                    it.copy(
                        chosenFee = event.fee
                    )
                }
                validateFeeEnough()
            }

            is TraderEvent.ShowCreateOfferDialog -> {
                _offerViewState.update { OfferViewState(dexieFee = it.dexieFee, offer = it.offer) }
                initCreateOfferUpdateParams(event.params)
                setEvent(TraderEvent.ShowCreateOfferDialog(event.params))
            }

            is TraderEvent.SendTakeOfferResult -> {
                JavaJSThreadCommunicator.resultTakeOffer = "SUCCESS"
                JavaJSThreadCommunicator.wait = false
                setLoading(false)
                setEvent(TraderEvent.SuccessTakingOffer)
            }

            is TraderEvent.SendCreateOfferResult -> {
                JavaJSThreadCommunicator.resultCreateOffer = hashMapOf()
                JavaJSThreadCommunicator.resultCreateOffer["offer"] = event.offer
                JavaJSThreadCommunicator.wait = false
                setEvent(TraderEvent.SuccessTakingOffer)
            }

            else -> {
                setEvent(event)
            }
        }
    }

    private fun initCreateOfferUpdateParams(params: CreateOfferParams) {
        viewModelScope.launch {
            requestedXCHAmount = .0
            _offerViewState.update {
                OfferViewState(
                    dexieFee = it.dexieFee,
                    offer = it.offer,
                    isLoading = true,
                    requested = listOf(),
                    offered = listOf()
                )
            }
            val requestJob = async {
                parseCreateOfferParamsRequested(
                    params = params.requestAssets ?: listOf()
                )
            }
            val offerJob = async {
                parseCreateOfferParamsOffered(
                    params = params.offerAssets ?: listOf()
                )
            }

            _offerViewState.update {
                it.copy(
                    offered = offerJob.await(),
                    requested = requestJob.await(),
                    acceptOffer = false,
                    isLoading = false,
                    btnEnabled = true
                )
            }

            saveSaveCoinsToCreateOffer(params.offerAssets)
        }
    }

    private suspend fun saveSaveCoinsToCreateOffer(offerAssets: List<AssetAmount>?) {
        if (wallet == null || offerAssets == null) return
        val spentCoins = mutableListOf<SpentCoin>()
        for (asset in offerAssets) {
            var tokenCode = "XCH"
            if (asset.assetId != "") tokenCode =
                tokenInteract.getTokenCodeByHash(asset.assetId ?: "")

            val coin = spentCoinsInteract.getSpentCoinsToPushTrans(
                wallet!!.networkType, wallet!!.address, tokenCode
            )
            spentCoins.addAll(coin)
        }
        _offerViewState.update { it.copy(spendCoins = spentCoins) }
    }

    fun updateOfferDialogState(offer: String) {
        _offerViewState.update { it.copy(offer = offer) }
    }

    fun updateTakingOffer(
        requestedJson: String, offeredJson: String
    ) {
        _offerViewState.update {
            OfferViewState(
                dexieFee = it.dexieFee,
                offer = it.offer,
                isLoading = true,
                requested = listOf(),
                offered = listOf()
            )
        }
        requestedXCHAmount = 0.0
        viewModelScope.launch {
            val offeredJob = async { parseTokenJson(offeredJson) }
            val requestedJob = async {
                parseTokenJson(
                    requestedJson, needSpendableBalance = true, needXCH = true
                )
            }

            val offered = offeredJob.await()
            val requested = requestedJob.await()
            var isEnough = true

            for (req in requested) {
                when (req) {
                    is NftToken -> {

                    }

                    is CatToken -> {
                        if (req.spendableBalance <= req.amount)
                            isEnough = false
                    }
                }
            }

            _offerViewState.update {
                it.copy(
                    acceptOffer = true,
                    offered = offered,
                    requested = requested,
                    btnEnabled = isEnough,
                    isLoading = false
                )
            }
            VLog.d("OfferViewState update : ${_offerViewState.value}")
        }
    }

    fun saveSpentCoins(spentCoins: String, timePended: Long) {
        viewModelScope.launch {
            val jsonObject = JSONObject(spentCoins)
            for (key in jsonObject.keys()) {
                val value = jsonObject[key]
                if (key == "null") {
                    spentCoinsInteract.insertSpentCoinsJson(
                        value.toString(), timePended, "XCH", wallet?.address.orEmpty()
                    )
                } else {
                    val code = tokenInteract.getTokenCodeByHash(key)
                    spentCoinsInteract.insertSpentCoinsJson(
                        value.toString(), timePended, code, wallet?.address.orEmpty()
                    )
                }
            }

            val acceptOffer = offerViewState.value.acceptOffer
            if (offerViewState.value.acceptOffer) {
                val usedNFT =
                    if (acceptOffer) offerViewState.value.requested
                    else offerViewState.value.offered
                for (req in usedNFT) {
                    if (req is NftToken) {
                        nftInteractor.updateNftInfoPendingTime(true, timePended, req.nftId)
                    }
                }
            }
        }
    }

    fun saveOfferTransaction(acceptOffer: Boolean): Long {
        VLog.d("Offer View State to save offer tran : ${offerViewState.value}")
        val tranID = UUID.randomUUID().toString()
        val timeCreated = System.currentTimeMillis()
        val offerTran = OfferTransaction(
            transId = tranID,
            createAtTime = timeCreated,
            requested = offerViewState.value.requested,
            offered = offerViewState.value.offered,
            source = "dexie.space",
            hashTransaction = "",
            fee = offerViewState.value.chosenFee,
            height = 0L,
            acceptOffer = acceptOffer
        )
        viewModelScope.launch {
            offerTransactionInteract.saveOfferTransaction(wallet!!.address, offerTran)
        }
        return timeCreated
    }

    private suspend fun parseTokenJson(
        json: String, needSpendableBalance: Boolean = false, needXCH: Boolean = false
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
                val nftInfo = blockChainInteract.getNftInfoByCoinID(
                    "Chia Network", coinID = assetID
                ) ?: continue
                val nftCoin = nftInteractor.getNFTCoinHashByNFTID(nftInfo.nftID)
                token = NftToken(
                    collection = nftInfo.collection,
                    nftId = nftInfo.nftID,
                    imgUrl = nftInfo.imageUrl,
                    nftCoinHash = nftCoin
                )
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
                if (needSpendableBalance) {
                    spendableBalance = spentCoinsInteract.getSpendableBalanceByTokenCode(
                        assetID, code, wallet?.address ?: ""
                    ).first()
                }

                if (code == "XCH" && needXCH) {
                    requestedXCHAmount = abs(dividedAmount)
                }

                token = CatToken(
                    assetID = assetID,
                    amount = abs(dividedAmount),
                    code = code,
                    spendableBalance = spendableBalance
                )
            }

            VLog.d("Adding Token Abstract : $token")
            list.add(token)
        }

        return list
    }


    private suspend fun parseCreateOfferParamsOffered(
        params: List<AssetAmount>
    ): List<Token> {
        val list = mutableListOf<Token>()

        for (par in params) {
            if (par.assetId == null) continue
            if (par.assetId.startsWith("nft")) {
                val collectionName = nftInteractor.getCollectionNameByNFTID(
                    networkItem = getNetworkItemFromPrefs(wallet!!.networkType) ?: continue,
                    nftID = par.assetId
                )
                val nftCoinHash = nftInteractor.getNFTCoinHashByNFTID(par.assetId)
                list.add(
                    NftToken(
                        collection = collectionName,
                        nftId = par.assetId,
                        imgUrl = "",
                        nftCoinHash = nftCoinHash
                    )
                )
            } else {
                var code = "XCH"
                if (par.assetId != "") {
                    code = tokenInteract.getTokenCodeByHash(par.assetId)
                }

                val amount = par.amount?.toDoubleOrNull() ?: 0.0

                var spendableBalance = 0.0
                spendableBalance = spentCoinsInteract.getSpendableBalanceByTokenCode(
                    par.assetId, code, wallet?.address ?: ""
                ).first()

                if (code == "XCH") {
                    requestedXCHAmount = amount
                }

                list.add(
                    CatToken(
                        assetID = par.assetId,
                        amount = amount,
                        code = code,
                        spendableBalance = spendableBalance
                    )
                )
            }
        }

        return list
    }


    private suspend fun parseCreateOfferParamsRequested(
        params: List<AssetAmount>, needSpendableBalance: Boolean = false, needXCH: Boolean = false
    ): List<Token> {
        val list = mutableListOf<Token>()

        for (par in params) {
            if (par.assetId == null) continue
            if (par.assetId.startsWith("nft")) {
                val collectionName = nftInteractor.getCollectionNameByNFTID(
                    getNetworkItemFromPrefs(wallet!!.networkType) ?: continue, par.assetId
                )
                list.add(
                    NftToken(
                        collection = collectionName, nftId = par.assetId, "", ""
                    )
                )
            } else {
                var code = "XCH"
                if (par.assetId != "") {
                    code = tokenInteract.getTokenCodeByHash(par.assetId)
                }

                val amount = par.amount?.toDoubleOrNull() ?: 0.0

                var spendableBalance = 0.0
                if (needSpendableBalance) {
                    spendableBalance = spentCoinsInteract.getSpendableBalanceByTokenCode(
                        par.assetId, code, wallet?.address ?: ""
                    ).first()
                }
                if (code == "XCH" && needXCH) {
                    requestedXCHAmount = amount
                }

                list.add(
                    CatToken(
                        assetID = par.assetId,
                        amount = amount,
                        code = code,
                        spendableBalance = spendableBalance
                    )
                )
            }
        }

        return list
    }

    private suspend fun getNetworkItemFromPrefs(networkType: String): NetworkItem? {
        val item = prefsManager.getObjectString(getPreferenceKeyForNetworkItem(networkType))
        if (item.isEmpty()) return null
        return Gson().fromJson(item, NetworkItem::class.java)
    }

    fun setLoading(isLoading: Boolean) {
        _viewState.update { it.copy(isLoading = isLoading) }
    }

    private fun validateFeeEnough() {
        val total = requestedXCHAmount + offerViewState.value.chosenFee
        _offerViewState.update { it.copy(feeEnough = total <= (wallet?.balance ?: 0.0)) }
    }

    suspend fun getNftCoinById(coinHash: String): NFTCoin? {
        return nftInteractor.getNFTCoinByHash(coinHash)
    }

    fun tempCleanUpOfferTranAndSpentCoins() {
        viewModelScope.launch {
            val offerTrans = offerTransactionInteract.getAllOfferTransactions()
            for (offer in offerTrans) {
                val affectedRows =
                    spentCoinsInteract.deleteSpentConsByTimeCreated(offer.createAtTime)
                VLog.d("Affected Rows after deleting : $affectedRows")
            }
        }
    }

    suspend fun getNftCoinFromWallet(nftCoinID: String) =
        nftInteractor.getNFTCoinByNFTIDFromWallet(
            getNetworkItemFromPrefs(wallet!!.networkType)!!,
            nftCoinID
        )

    fun disableDApp() {
        connectedDApp.connected.remove("Dexie")
    }

}