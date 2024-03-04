package com.green.wallet.presentation.main.transaction.btmCancel

import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.green.wallet.data.network.dto.greenapp.network.NetworkItem
import com.green.wallet.data.preference.PrefsManager
import com.green.wallet.domain.domainmodel.OfferTransaction
import com.green.wallet.domain.domainmodel.TransferTransaction
import com.green.wallet.domain.domainmodel.Wallet
import com.green.wallet.domain.interact.BlockChainInteract
import com.green.wallet.domain.interact.DexieInteract
import com.green.wallet.domain.interact.OfferTransactionInteract
import com.green.wallet.domain.interact.SpentCoinsInteract
import com.green.wallet.domain.interact.WalletInteract
import com.green.wallet.presentation.custom.getPreferenceKeyForNetworkItem
import com.green.wallet.presentation.main.transaction.btmSpeedy.SpeedyTokenEvent
import com.green.wallet.presentation.tools.Resource
import com.green.wallet.presentation.tools.VLog
import com.greenwallet.core.base.BaseViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CancelOfferViewModel @Inject constructor(
    private val offerTransactionInteract: OfferTransactionInteract,
    private val dexieInteract: DexieInteract,
    private val walletInteract: WalletInteract,
    private val coinsInteract: SpentCoinsInteract,
    private val prefsManager: PrefsManager,
    private val blockChainInteract: BlockChainInteract,
) : BaseViewModel<CancelOfferState, CancelOfferEvent>(CancelOfferState()) {

    var offerTransaction: OfferTransaction? = null
    lateinit var wallet: Wallet

    private val handler = CoroutineExceptionHandler { _, ex ->
        VLog.d("Handler on cancelOfferVM : ${ex.message}")
    }

    fun initOfferTransaction(tranID: String) {
        viewModelScope.launch(handler) {
            offerTransaction = offerTransactionInteract.getOfferTransactionByTranID(tranID)
            if (offerTransaction == null) {
                VLog.d("OfferTransaction with tranID : $tranID is null")
            }
            _viewState.update { it.copy(addressFk = offerTransaction!!.addressFk) }

            initWallet(offerTransaction!!.addressFk)
            getSpentCoinsSum()
        }

        viewModelScope.launch {
            val dexieRecommendedFee = dexieInteract.getDexieMinFee().recommended
            _viewState.update {
                it.copy(normalFeeDexie = dexieRecommendedFee, fee = dexieRecommendedFee)
            }
        }
    }

    private suspend fun initWallet(addressFK: String) {
        wallet = walletInteract.getWalletByAddress(addressFK)
    }

    fun handleEvent(event: CancelOfferEvent) {
        when (event) {
            is CancelOfferEvent.OnFeeChosen -> {
                _viewState.update { it.copy(fee = event.amount) }
            }

            is CancelOfferEvent.OnSign -> {
                setEvent(CancelOfferEvent.OnSign)
                _viewState.update { it.copy(isLoading = true) }
            }
        }
    }

    suspend fun getNetworkItemFromPrefs(networkType: String): NetworkItem? {
        val item = prefsManager.getObjectString(getPreferenceKeyForNetworkItem(networkType))
        if (item.isEmpty()) return null
        return Gson().fromJson(item, NetworkItem::class.java)
    }

    fun getDestinationHash() = wallet.puzzle_hashes[1]

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
                sendAmount = -1.0,
                networkType = wallet.networkType,
                fingerPrint = wallet.fingerPrint,
                code = "XCH",
                destPuzzleHash = "",
                address = wallet.address,
                fee = viewState.value.fee,
                spentCoinsJson = spentCoinsJson,
                spentCoinsToken = spentTokensJson
            )
            when (result.state) {
                Resource.State.SUCCESS -> {
                    setLoading(false)
                }

                Resource.State.ERROR -> {
                    setLoading(false)
                }

                else -> Unit
            }
        }
    }

    suspend fun getTranXCHCoins() = withContext(Dispatchers.IO) {
        val tranCoins =
            coinsInteract.getSpentCoinsByTransactionTimeCode(offerTransaction!!.createAtTime, "XCH")
        tranCoins
    }

    suspend fun getSpentCoins() = withContext(Dispatchers.IO) {
        coinsInteract.getSpentCoinsToPushTrans(wallet.networkType, wallet.address, "XCH")
    }


    private suspend fun getSpentCoinsSum() {
        coinsInteract.getSpentCoinsBalanceByAddressAndCode(wallet.address, "XCH")
            .collectLatest { it ->
                val feeSpendable = wallet.balance - it
                VLog.d("Wallet Balance : ${wallet.balance}, Spent Coins Fee : $it")
                _viewState.update {
                    it.copy(
                        spendableBalance = Math.max(feeSpendable, 0.0)
                    )
                }
            }
    }

    fun setLoading(isLoading: Boolean) {
        _viewState.update { it.copy(isLoading = isLoading) }
    }

}