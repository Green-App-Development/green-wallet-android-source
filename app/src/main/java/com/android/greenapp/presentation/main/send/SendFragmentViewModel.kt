package com.android.greenapp.presentation.main.send

import androidx.lifecycle.ViewModel
import com.android.greenapp.domain.entity.Address
import com.android.greenapp.domain.interact.*
import com.android.greenapp.presentation.tools.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Created by bekjan on 22.04.2022.
 * email: bekjan.omirzak98@gmail.com
 */


class SendFragmentViewModel @Inject constructor(
    private val walletInteract: WalletInteract,
    private val blockChainInteract: BlockChainInteract,
    private val addressInteract: AddressInteract,
    private val greenAppInteract: GreenAppInteract,
    private val cryptocurrencyInteract: CryptocurrencyInteract,
    private val tokenInteract: TokenInteract
) :
    ViewModel() {

    private val _sendTransResponse = MutableStateFlow<Resource<String>>(Resource.loading())
    val sendTransResponse = _sendTransResponse.asStateFlow()
    private var sendTransJob: Job? = null

    suspend fun getDistinctNetworkTypeValues() = walletInteract.getDistinctNetworkTypes()

    suspend fun queryWalletList(type: String, fingerPrint: Long?) =
        walletInteract.getWalletListByNetworkTypeFingerPrint(type, fingerPrint)

    suspend fun queryWalletWithTokensList(type: String, fingerPrint: Long?) =
        walletInteract.getWalletWithTokensByFingerPrintNetworkType(fingerPrint, type)

    suspend fun push_transaction(spendBundle: String,url: String,amount:Double,networkType: String,fingerPrint: Long,code:String)=blockChainInteract.push_tx(spendBundle,url,amount,networkType,fingerPrint,code)

    suspend fun checkIfAddressExistInDb(address: String) =
        addressInteract.checkIfAddressAlreadyExist(address = address)

    suspend fun getCoinDetailsFeeCommission(code: String) =
        greenAppInteract.getCoinDetails(code).fee_commission


    suspend fun getTokenPriceByCode(code: String) = tokenInteract.getTokenPriceByCode(code)

    suspend fun insertAddressEntity(address: Address) = addressInteract.insertAddressEntity(address)

}
