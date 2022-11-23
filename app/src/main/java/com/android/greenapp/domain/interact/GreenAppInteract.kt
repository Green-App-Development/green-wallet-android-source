package com.android.greenapp.domain.interact

import com.android.greenapp.data.network.dto.greenapp.language.LanguageItem
import com.android.greenapp.data.network.dto.greenapp.network.NetworkItem
import com.android.greenapp.domain.domainmodel.CoinDetails
import com.android.greenapp.presentation.tools.Resource

/**
 * Created by bekjan on 06.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */
interface GreenAppInteract {


	suspend fun getAvailableLanguageList(): Resource<List<LanguageItem>>

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
