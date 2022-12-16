package extralogic.wallet.greenapp.presentation.main.createnewwallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import extralogic.wallet.greenapp.domain.domainmodel.Wallet
import extralogic.wallet.greenapp.domain.interact.BlockChainInteract
import extralogic.wallet.greenapp.domain.interact.CryptocurrencyInteract
import extralogic.wallet.greenapp.domain.interact.GreenAppInteract
import extralogic.wallet.greenapp.domain.interact.TokenInteract
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by bekjan on 07.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */

class NewWalletViewModel @Inject constructor(
	private val blockChainInteract: BlockChainInteract,
	private val greenAppInteract: GreenAppInteract,
	private val tokenInteract: TokenInteract,
	private val cryptocurrencyInteract: CryptocurrencyInteract
) :
	ViewModel() {

	init {
		viewModelScope.launch {
			cryptocurrencyInteract.getAllTails()
		}
	}

	fun createNewWallet(
		wallet: Wallet,
		callBack: () -> Unit
	) = viewModelScope.launch {
		blockChainInteract.saveNewWallet(
			wallet = wallet,
			imported = false
		)
		callBack()
	}

	suspend fun getTokenDefaultOnMainScreen() = tokenInteract.getTokenListDefaultOnMainScreen()

	suspend fun getCoinDetails(coinCode: String) = greenAppInteract.getCoinDetails(coinCode)

}
