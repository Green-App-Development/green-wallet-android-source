package com.green.wallet.presentation.main.walletsettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common.tools.VLog
import com.green.wallet.domain.interact.WalletInteract
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by bekjan on 30.11.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class WalletSettingsViewModel @Inject constructor(private val walletInteract: WalletInteract) :
	ViewModel() {


	suspend fun getWalletByAddress(address: String) = walletInteract.getWalletByAddress(address)
	private val handler = CoroutineExceptionHandler { context, throwable ->
		VLog.d("Exception in wallet settings view model : ${throwable.message}")
	}

	fun updateHashesMainToken(
		address: String,
		main_hashes: List<String>,
		hashListImported: HashMap<String, List<String>>,
		observer: Int,
		nonObserver: Int
	) {
		viewModelScope.launch(handler) {
			walletInteract.updateHashListImported(
				address,
				main_hashes,
				hashListImported,
				observer,
				nonObserver
			)
		}
	}

}
