package com.green.wallet.presentation.main

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.green.wallet.data.preference.PrefsManager
import com.green.wallet.domain.interact.CryptocurrencyInteract
import com.green.wallet.domain.interact.GreenAppInteract
import com.green.wallet.domain.interact.PrefsInteract
import com.green.wallet.domain.interact.SupportInteract
import com.green.wallet.domain.interact.TibetInteract
import com.green.wallet.domain.interact.WalletInteract
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


class MainViewModel @Inject constructor(
	private val prefs: PrefsInteract,
	private val walletInteract: WalletInteract,
	private val greenAppInteract: GreenAppInteract,
	private val supportInteract: SupportInteract,
	private val cryptoInteract:CryptocurrencyInteract,
	private val tibetInteract: TibetInteract
) : ViewModel() {

	init {
		oneTimeRequestEachApplication()
		requestPerApplication()
	}

		private var updateCoinDetailsJob: Job? = null

	private fun oneTimeRequestEachApplication() {
		updateCoinDetailsJob?.cancel()
		updateCoinDetailsJob = viewModelScope.launch {
			val curLangCode = prefs.getSettingString(PrefsManager.CUR_LANGUAGE_CODE, "")
			if (curLangCode.isNotEmpty()) {
				greenAppInteract.downloadLanguageTranslate(curLangCode)
			}
		}
	}

	private val _show_data_wallet = MutableStateFlow(false)
	val show_data_wallet = _show_data_wallet.asStateFlow()
	private val _money_send = MutableStateFlow(false)
	val money_send_success = _money_send.asStateFlow()
	private val _decodeQrCode = MutableStateFlow("")
	val decodedQrCode: StateFlow<String> = _decodeQrCode
	private val _delete_wallet = MutableStateFlow(false)
	val delete_wallet: StateFlow<Boolean> = _delete_wallet.asStateFlow()
	private val _chosenAddress = MutableStateFlow("")
	val chosenAddress: StateFlow<String> = _chosenAddress.asStateFlow()


	fun saveDecodeQrCode(code: String) {
		viewModelScope.launch {
			_decodeQrCode.emit(code)
		}
	}

	suspend fun getDistinctNetworkTypes() = walletInteract.getDistinctNetworkTypes()

	suspend fun getNightModeIsOn() = prefs.getSettingBoolean(PrefsManager.NIGHT_MODE_ON, true)
	suspend fun getPushNotifIsOn() = prefs.getSettingBoolean(PrefsManager.PUSH_NOTIF_IS_ON, true)

	suspend fun getLastVisitedValue() = prefs.getSettingLong(PrefsManager.LAST_VISITED, 0L)

	suspend fun getServerTime() = greenAppInteract.getServerTime()

	suspend fun saveTimeDifference(timeDif: Long) =
		prefs.saveSettingLong(PrefsManager.TIME_DIFFERENCE, timeDif)

	fun saveNightIsOn(nightMode: Boolean) {
		viewModelScope.launch {
			with(prefs) {
				saveSettingLong(PrefsManager.LAST_VISITED, System.currentTimeMillis())
				saveSettingBoolean(PrefsManager.PREV_MODE_CHANGED, value = true)
				saveSettingBoolean(PrefsManager.NIGHT_MODE_ON, value = nightMode)
			}
			AppCompatDelegate.setDefaultNightMode(if (nightMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
		}
	}

	fun savePushNotifIsOn(value: Boolean) {
		viewModelScope.launch {
			prefs.saveSettingBoolean(PrefsManager.PUSH_NOTIF_IS_ON, value = value)
		}
	}

	fun saveBalanceIsHidden(hidden: Boolean) {
		viewModelScope.launch {
			prefs.saveSettingBoolean(PrefsManager.BALANCE_IS_HIDDEN, hidden)
		}
	}

	fun showDataWalletInvisible() {
		viewModelScope.launch {
			_show_data_wallet.emit(value = false)
		}
	}

	fun sendMoneyFalse() {
		viewModelScope.launch {
			_money_send.value = false
		}
	}

	fun sendMoneySuccess() {
		_money_send.value = true
	}

	fun showDataWalletVisible() {
		viewModelScope.launch {
			delay(500)
			_show_data_wallet.emit(value = true)
		}
	}

	fun deleteWalletFalse() {
		viewModelScope.launch {
			_delete_wallet.emit(value = false)
		}
	}

	fun deleteWalletTrue() {
		viewModelScope.launch {
			_delete_wallet.emit(value = true)
		}
	}


	fun saveLastVisitedLongValue(value: Long) {
		viewModelScope.launch {
			prefs.saveSettingLong(PrefsManager.LAST_VISITED, value = value)
		}
	}

	fun saveChosenAddress(address: String) {
		viewModelScope.launch {
			_chosenAddress.emit(address)
		}
	}

	suspend fun getAvailableNetworkItems() =
		greenAppInteract.getAllNetworkItemsListFromPrefs()
	
	suspend fun getWalletSizeInDB() = walletInteract.getAllWalletList()

	private fun requestPerApplication(){
		viewModelScope.launch {
			with(greenAppInteract) {
				getAvailableNetworkItemsFromRestAndSave()
				getAvailableLanguageList()
				getVerifiedDidList()
				getAgreementsText()
				updateCoinDetails()
			}
			supportInteract.getFAQQuestionAnswers()
			with(cryptoInteract) {
				getAllTails()
				checkingDefaultWalletTails()
			}
			with(tibetInteract) {
				saveTokensPairID()
			}
		}
	}

}
