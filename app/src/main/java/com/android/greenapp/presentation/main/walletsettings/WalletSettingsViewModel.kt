package com.android.greenapp.presentation.main.walletsettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.greenapp.domain.interact.WalletInteract
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by bekjan on 30.11.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class WalletSettingsViewModel @Inject constructor(private val walletInteract: WalletInteract) :
	ViewModel() {


	suspend fun getWalletByAddress(address: String) = walletInteract.getWalletByAddress(address)

	fun updateHashesMainToken(
		address: String,
		main_hashes: List<String>,
		hashListImported: HashMap<String, List<String>>,
		observer:Int,
		nonObserver:Int
	) {
		viewModelScope.launch {
			walletInteract.updateHashListImported(address, main_hashes, hashListImported,observer,nonObserver)
		}
	}

}
