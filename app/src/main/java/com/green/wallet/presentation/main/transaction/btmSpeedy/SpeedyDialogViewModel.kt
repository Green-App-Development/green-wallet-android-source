package com.green.wallet.presentation.main.transaction.btmSpeedy

import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.green.wallet.data.network.dto.coinSolution.Coin
import com.green.wallet.data.network.dto.greenapp.network.NetworkItem
import com.green.wallet.data.preference.PrefsManager
import com.green.wallet.domain.domainmodel.SpentCoin
import com.green.wallet.domain.domainmodel.Transaction
import com.green.wallet.domain.domainmodel.Wallet
import com.green.wallet.domain.interact.BlockChainInteract
import com.green.wallet.domain.interact.NFTInteract
import com.green.wallet.domain.interact.PrefsInteract
import com.green.wallet.domain.interact.SpentCoinsInteract
import com.green.wallet.domain.interact.TokenInteract
import com.green.wallet.domain.interact.TransactionInteract
import com.green.wallet.domain.interact.WalletInteract
import com.green.wallet.presentation.custom.getPreferenceKeyForNetworkItem
import com.green.wallet.presentation.main.dapp.trade.models.CatToken
import com.green.wallet.presentation.main.dapp.trade.models.NftToken
import com.green.wallet.presentation.tools.Resource
import com.green.wallet.presentation.tools.VLog
import com.greenwallet.core.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
    private val prefs: PrefsInteract
) : BaseViewModel<SpeedyTokenState, SpeedyTokenEvent>(SpeedyTokenState()) {

    private lateinit var transaction: Transaction
    lateinit var wallet: Wallet

    init {
        viewModelScope.launch {
            delay(1000L)
            getTranCoins()
        }
    }

    fun setCurTransaction(tran: Transaction?) {
        VLog.d("Setting up transaction property : $tran")
        tran?.let {
            this.transaction = tran
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
            }

            else -> Unit
        }
    }

    private suspend fun getInfoAboutTransaction() {
        if (transaction.code == "NFT") {
            val nftInfo = nftInteract.getNftINFOByHash(transaction.nftCoinHash)
            VLog.d("NftInfo Entity from DB : $nftInfo")
            _viewState.update {
                it.copy(
                    token = NftToken(
                        collection = nftInfo.collection,
                        nftId = nftInfo.nft_id,
                        imgUrl = nftInfo.data_url
                    )
                )
            }
        } else {
            val token = tokenInteract.getTokenByCode(transaction.code) ?: return
            _viewState.update {
                it.copy(
                    token = CatToken(
                        transaction.code,
                        assetID = token.hash,
                        transaction.amount
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
                    _viewState.update { it.copy(spendableFee = feeSpendable) }
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
                    transactionInteract.deleteTransByID(transaction.transactionId)
                }

                Resource.State.ERROR -> {

                }

                Resource.State.LOADING -> {

                }
            }
        }
    }

}