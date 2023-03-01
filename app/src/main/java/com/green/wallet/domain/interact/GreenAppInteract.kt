package com.green.wallet.domain.interact

import com.green.wallet.data.network.dto.greenapp.lang.LanguageItemDto
import com.green.wallet.data.network.dto.greenapp.network.NetworkItem
import com.green.wallet.domain.domainmodel.CoinDetails
import com.green.wallet.presentation.tools.Resource


interface GreenAppInteract {


	suspend fun getAvailableLanguageList(): Resource<List<LanguageItemDto>>

	suspend fun downloadLanguageTranslate(langCode: String): Resource<String>

	suspend fun changeLanguageIsSavedBefore()

	suspend fun getAvailableNetworkItemsFromRestAndSave()

	suspend fun getAllNetworkItemsListFromPrefs(): Resource<List<NetworkItem>>

	suspend fun requestOtherNotifItems()

	suspend fun getAgreementsText(): Resource<String>

	suspend fun updateCoinDetails()

	suspend fun getCoinDetails(code: String): CoinDetails

	suspend fun getServerTime(): Long


}
