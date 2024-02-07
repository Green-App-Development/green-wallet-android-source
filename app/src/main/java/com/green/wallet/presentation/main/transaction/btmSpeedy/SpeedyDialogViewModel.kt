package com.green.wallet.presentation.main.transaction.btmSpeedy

import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.green.wallet.data.network.dto.greenapp.network.NetworkItem
import com.green.wallet.data.preference.PrefsManager
import com.green.wallet.domain.domainmodel.NFTCoin
import com.green.wallet.domain.domainmodel.NFTInfo
import com.green.wallet.domain.domainmodel.Transaction
import com.green.wallet.domain.domainmodel.Wallet
import com.green.wallet.domain.interact.BlockChainInteract
import com.green.wallet.domain.interact.DexieInteract
import com.green.wallet.domain.interact.NFTInteract
import com.green.wallet.domain.interact.PrefsInteract
import com.green.wallet.domain.interact.SpentCoinsInteract
import com.green.wallet.domain.interact.TokenInteract
import com.green.wallet.domain.interact.TransactionInteract
import com.green.wallet.domain.interact.WalletInteract
import com.green.wallet.presentation.custom.getPreferenceKeyForNetworkItem
import com.green.wallet.presentation.main.dapp.trade.models.CatToken
import com.green.wallet.presentation.main.dapp.trade.models.NftToken
import com.green.wallet.presentation.main.enterpasscode.PassCodeCommunicator
import com.green.wallet.presentation.main.pincode.PinCodeCommunicator
import com.green.wallet.presentation.tools.ReasonEnterCode
import com.green.wallet.presentation.tools.Resource
import com.green.wallet.presentation.tools.VLog
import com.greenwallet.core.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SpeedyDialogViewModel @Inject constructor(
    private val tokenInteract: TokenInteract,
    private val nftInteract: NFTInteract,
    private val walletInteract: WalletInteract,
    private val prefsManager: PrefsManager,
    private val coinsInteract: SpentCoinsInteract,
    private val blockChainInteract: BlockChainInteract,
    private val transactionInteract: TransactionInteract,
    private val prefs: PrefsInteract,
    private val dexieInteract: DexieInteract,
    pinCodeCommunicator: PinCodeCommunicator
) : BaseViewModel<SpeedyTokenState, SpeedyTokenEvent>(SpeedyTokenState()) {

    private lateinit var transaction: Transaction
    lateinit var wallet: Wallet
    lateinit var nftCoin: NFTCoin
    lateinit var nftInfo: NFTInfo

    init {
        getDexieFeeInteract()

        pinCodeCommunicator.onSuccessPassCode = {
            when (it) {
                ReasonEnterCode.SPEEDY_TRAN -> {
                    setEvent(SpeedyTokenEvent.ConfirmedPinCode)
                }

                else -> Unit
            }
        }
    }

    private fun getDexieFeeInteract() {
        viewModelScope.launch {
            val dexieRecommendedFee = dexieInteract.getDexieMinFee().recommended
            _viewState.update {
                it.copy(normalFeeDexie = dexieRecommendedFee, fee = dexieRecommendedFee)
            }
            validateEnoughFee()
            VLog.d("Validating amount after dexie : ${viewState.value}")
        }
    }

    fun setCurTransaction(tran: Transaction?) {
        VLog.d("Setting up transaction property : $tran")
        tran?.let {
            this.transaction = tran
            _viewState.update { it.copy(address = tran.fkAddress) }
            viewModelScope.launch(Dispatchers.IO) {
                getInfoAboutTransaction()
                wallet =
                    walletInteract.getWalletByAddress(tran.fkAddress)
                getSpentCoinsSum()
            }
        }
    }

    fun handleEvent(event: SpeedyTokenEvent) {
        setEvent(event)
        when (event) {
            is SpeedyTokenEvent.OnFeeChosen -> {
                _viewState.update { it.copy(fee = event.fee) }
                validateEnoughFee()
            }

            else -> Unit
        }
    }

    private suspend fun getInfoAboutTransaction() {
        if (transaction.code == "NFT") {
            this.nftInfo = nftInteract.getNftINFOByHash(transaction.nftCoinHash)
            this.nftCoin = nftInteract.getNFTCoinByHash(nftInfo.nft_coin_hash) ?: return
            VLog.d("NftInfo Entity from DB : $nftInfo")
            _viewState.update {
                it.copy(
                    token = NftToken(
                        collection = this.nftInfo.collection,
                        nftId = this.nftInfo.nft_id,
                        imgUrl = this.nftInfo.data_url
                    )
                )
            }
        } else {
            val token = tokenInteract.getTokenByCode(transaction.code) ?: return
            _viewState.update {
                it.copy(
                    token = CatToken(
                        code = transaction.code,
                        assetID = token.hash,
                        amount = transaction.amount
                    )
                )
            }
        }
    }

    suspend fun getNetworkItemFromPrefs(networkType: String): NetworkItem? {
        val item = prefsManager.getObjectString(getPreferenceKeyForNetworkItem(networkType))
        if (item.isEmpty()) return null
        return Gson().fromJson(item, NetworkItem::class.java)
    }

    suspend fun getTranCoins() = withContext(Dispatchers.IO) {
        val tranCoins = coinsInteract.getSpentCoinsByTransactionTime(
            transaction.createdAtTime - prefs.getSettingLong(
                PrefsManager.TIME_DIFFERENCE,
                System.currentTimeMillis()
            )
        )
        VLog.d(
            "Transaction Coins for tran : $tranCoins time : ${
                transaction.createdAtTime - prefs.getSettingLong(
                    PrefsManager.TIME_DIFFERENCE,
                    System.currentTimeMillis()
                )
            }"
        )
        tranCoins
    }

    suspend fun getSpentCoins() = withContext(Dispatchers.IO) {
        coinsInteract.getSpentCoinsToPushTrans(wallet.networkType, wallet.address, "XCH")
    }

    private fun getSpentCoinsSum() {
        viewModelScope.launch {
            coinsInteract.getSpentCoinsBalanceByAddressAndCode(wallet.address, "XCH")
                .collectLatest { it ->
                    val feeSpendable = wallet.balance - it
                    VLog.d("Wallet Balance : ${wallet.balance}, Spent Coins Fee : $it")
                    _viewState.update {
                        it.copy(
                            spendableBalance = feeSpendable
                        )
                    }
                    validateEnoughFee()
                }
        }
    }

    fun burstTransaction(
        spendBundleJson: String,
        spentCoinsJson: String,
        spentTokensJson: String,
        url: String
    ) {
        viewModelScope.launch {
            setLoading(true)
            val result = blockChainInteract.push_tx(
                jsonSpendBundle = spendBundleJson,
                url = url,
                sendAmount = transaction.amount,
                networkType = wallet.networkType,
                fingerPrint = wallet.fingerPrint,
                code = transaction.code,
                destPuzzleHash = transaction.toDestHash,
                address = wallet.address,
                fee = viewState.value.fee,
                spentCoinsJson = spentCoinsJson,
                spentCoinsToken = spentTokensJson
            )
            when (result.state) {
                Resource.State.SUCCESS -> {
                    setLoading(false)
                    transactionInteract.deleteTransByID(transaction.transactionId)
                    setEvent(SpeedyTokenEvent.OnSpeedSuccess)
                }

                Resource.State.ERROR -> {
                    setLoading(false)
                    setEvent(SpeedyTokenEvent.OnSpeedError)
                }

                else -> Unit
            }
        }
    }

    fun burstTransactionNFT(spentBundleJson: String, spentCoinsJson: String, url: String) {
        viewModelScope.launch {
            setLoading(true)
            val res = blockChainInteract.push_tx_nft(
                spentBundleJson,
                url,
                transaction.toDestHash,
                nftInfo,
                spentCoinsJson,
                viewState.value.fee,
                nftCoin.confirmedBlockIndex.toInt(),
                wallet.networkType
            )
            when (res.state) {
                Resource.State.SUCCESS -> {
                    transactionInteract.deleteTransByID(transaction.transactionId)
                    setLoading(false)
                    setEvent(SpeedyTokenEvent.OnSpeedSuccess)
                }

                Resource.State.ERROR -> {
                    setLoading(false)
                    setEvent(SpeedyTokenEvent.OnSpeedError)
                }

                else -> Unit
            }
        }
    }

    private fun validateEnoughFee() {
        _viewState.update { it.copy(isChosenFeeEnough = it.fee <= it.spendableBalance) }
    }

    fun setLoading(isLoading: Boolean) {
        _viewState.update { it.copy(isLoading = isLoading) }
    }

}