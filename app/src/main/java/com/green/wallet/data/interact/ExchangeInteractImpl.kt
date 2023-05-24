package com.green.wallet.data.interact

import com.green.wallet.data.network.ExchangeService
import com.green.wallet.data.preference.PrefsManager
import com.green.wallet.domain.interact.ExchangeInteract
import com.green.wallet.domain.interact.PrefsInteract
import com.green.wallet.presentation.tools.VLog

class ExchangeInteractImpl(
	private val exchangeService: ExchangeService,
	private val prefsInteract: PrefsInteract
) : ExchangeInteract {


	override suspend fun createExchangeRequest(
		give_address: String,
		give_amount: Double,
		get_address: String
	) {
		try {
			val guid = prefsInteract.getSettingString(PrefsManager.USER_GUID, "")
			
		} catch (ex: Exception) {
			VLog.d("Exception in creating exchange requesting : ${ex.message}")
		}
	}


}
