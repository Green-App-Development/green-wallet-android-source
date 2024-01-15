package com.green.wallet.presentation.main.send

import androidx.lifecycle.viewModelScope
import com.green.wallet.domain.domainmodel.Address
import com.green.wallet.domain.interact.*
import com.green.wallet.presentation.tools.Resource
import com.greenwallet.core.base.BaseViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


class SendFragmentViewModel @Inject constructor(
    private val walletInteract: WalletInteract,
    private val blockChainInteract: BlockChainInteract,
    private val addressInteract: AddressInteract,
    private val greenAppInteract: GreenAppInteract,
    private val tokenInteract: TokenInteract,
    private val spentCoinsInteract: SpentCoinsInteract,
    private val dexieInteract: DexieInteract
) : BaseViewModel<TransferState, TransferEvent>(TransferState()) {

    suspend fun getDistinctNetworkTypeValues() = walletInteract.getDistinctNetworkTypes()

    fun queryWalletWithTokensList(type: String, fingerPrint: Long?) =
        walletInteract.getWalletWithTokensByFingerPrintNetworkTypeFlow(fingerPrint, type)

    fun pushTransaction(
        spendBundle: String,
        url: String,
        amount: Double,
        networkType: String,
        fingerPrint: Long,
        code: String,
        destPuzzleHash: String,
        address: String,
        fee: Double,
        spentCoinsJson: String,
        spentCoinsToken: String
    ) {
        viewModelScope.launch {
            setLoading(true)
            val result = blockChainInteract.push_tx(
                spendBundle,
                url,
                amount,
                networkType,
                fingerPrint,
                code,
                destPuzzleHash,
                address,
                fee,
                spentCoinsJson,
                spentCoinsToken
            )

            when (result.state) {
                Resource.State.SUCCESS -> {
                    setLoading(false)
                    setEvent(TransferEvent.OnSuccessTransfer)
                }

                Resource.State.ERROR -> {
                    setLoading(false)
                    setEvent(TransferEvent.OnErrorTransfer)
                }

                Resource.State.LOADING -> Unit
            }
        }
    }

    suspend fun checkIfAddressExistInDb(address: String) =
        addressInteract.checkIfAddressAlreadyExist(address = address)

    suspend fun getCoinDetailsFeeCommission(code: String) =
        greenAppInteract.getCoinDetails(code).fee_commission


    suspend fun getTokenPriceByCode(code: String) = tokenInteract.getTokenPriceByCode(code)

    suspend fun insertAddressEntity(address: Address) = addressInteract.insertAddressEntity(address)

    fun getSpentCoinsAmountsAddressCodeForSpendableBalance(
        address: String,
        tokenCode: String,
        networkType: String
    ) =
        spentCoinsInteract.getSumSpentCoinsForSpendableBalance(networkType, address, tokenCode)


    suspend fun swipedRefreshLayout(onFinished: () -> Unit) {
        blockChainInteract.updateBalanceAndTransactionsPeriodically()
        greenAppInteract.requestOtherNotifItems()
    }

    suspend fun getSpentCoinsToPushTrans(networkType: String, address: String, tokenCode: String) =
        spentCoinsInteract.getSpentCoinsToPushTrans(networkType, address, tokenCode)

    suspend fun getDexieFee() = dexieInteract.getDexieMinFee()

    private fun setLoading(loading: Boolean) {
        _viewState.update { it.copy(isLoading = loading) }
    }

}
