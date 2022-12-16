package extralogic.wallet.greenapp.presentation.main.receive

import androidx.lifecycle.ViewModel
import extralogic.wallet.greenapp.domain.interact.WalletInteract
import javax.inject.Inject

/**
 * Created by bekjan on 15.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class ReceiveViewModel @Inject constructor(private val walletInteract: WalletInteract) :
    ViewModel() {


    suspend fun getWalletListHomeIsAddedByNetworkType(
        networkType: String,
        fingerPrint: Long?
    ) =
        walletInteract.getWalletListByNetworkTypeFingerPrint(networkType = networkType, fingerPrint)


    suspend fun getDistinctNetworkTypeValues() = walletInteract.getDistinctNetworkTypes()


}