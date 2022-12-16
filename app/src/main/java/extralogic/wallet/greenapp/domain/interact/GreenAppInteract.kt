package extralogic.wallet.greenapp.domain.interact

import extralogic.wallet.greenapp.data.network.dto.greenapp.lang.LanguageItemDto
import extralogic.wallet.greenapp.data.network.dto.greenapp.network.NetworkItem
import extralogic.wallet.greenapp.domain.domainmodel.CoinDetails
import extralogic.wallet.greenapp.presentation.tools.Resource

/**
 * Created by bekjan on 06.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */
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
