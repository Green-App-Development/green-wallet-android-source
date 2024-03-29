package com.green.wallet.presentation.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.green.wallet.data.preference.PrefsManager
import com.green.wallet.domain.domainmodel.CurrencyItem
import com.green.wallet.domain.interact.*
import com.green.wallet.presentation.tools.VLog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


class HomeFragmentViewModel @Inject constructor(
	private val prefs: PrefsInteract,
	private val walletInteract: WalletInteract, private val cryptocurrencyInteract: CryptocurrencyInteract,
	private val blockChainInteract: BlockChainInteract,
	private val greenAppInteract: GreenAppInteract
) : ViewModel() {

	private var job10Seconds: Job? = null

	init {

	}

	private val handler = CoroutineExceptionHandler { _, ex ->
		VLog.d("Exception in homefragmentViewModel scope : ${ex}")
	}

	private var cryptoUpdateCourseJob: Job? = null


	private val _curCryptoCourse = MutableStateFlow<CurrencyItem?>(null)
	val curCryptoCourse: StateFlow<CurrencyItem?> = _curCryptoCourse

	suspend fun prevModeChanged() =
		prefs.getSettingBoolean(PrefsManager.PREV_MODE_CHANGED, default = false)


	suspend fun flowBalanceIsHidden() =
		prefs.getSettingBooleanFlow(PrefsManager.BALANCE_IS_HIDDEN, false)


	fun savePrevModeChanged(changed: Boolean) = viewModelScope.launch {
		prefs.saveSettingBoolean(PrefsManager.PREV_MODE_CHANGED, changed)
	}

	fun saveHomeIsAddedWalletCounter(counter: Int) {
		viewModelScope.launch {
			prefs.saveSettingInt(PrefsManager.HOME_ADDED_COUNTER, counter)
		}
	}

	fun getHomeAddedWalletWithTokensFlow() = walletInteract.getHomeAddedWalletWithTokensFlow()

	fun changeCryptCourseEvery10Seconds() {
		job10Seconds?.cancel()
		job10Seconds = viewModelScope.launch {
			var network = "Chia Network"
			while (true) {
//				updateCryptoCurrencyCourse(network)
				if (network == "Chia Network") {
					network = "Chives Network"
				} else {
					network = "Chia Network"
				}
				delay(10000)
			}
		}
	}

	fun updateCryptoCurrencyCourse(networkType: String) {
		cryptoUpdateCourseJob?.cancel()
		cryptoUpdateCourseJob = viewModelScope.launch(handler) {
			cryptocurrencyInteract.getCurrentCurrencyCourseByNetwork(networkType).collect {
				VLog.d("Emitting CourseValue for : $networkType and value : ${it.price}")
				it.network = networkType
				_curCryptoCourse.emit(it)
			}
		}
	}

	fun swipedRefreshClicked(onFinished: () -> Unit) {
		viewModelScope.launch(Dispatchers.IO) {
			blockChainInteract.updateBalanceAndTransactionsPeriodically()
			greenAppInteract.requestOtherNotifItems()
			withContext(Dispatchers.Main) {
				onFinished()
			}
		}
	}


}
