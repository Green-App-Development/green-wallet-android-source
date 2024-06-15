package com.green.wallet.data.interact

import com.green.wallet.data.network.SearchService
import com.green.wallet.domain.interact.NamesXCHDaoInteract
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject

class NamesXCHDaoInteractImpl @Inject constructor(
    private val service: SearchService
) : NamesXCHDaoInteract {

    override suspend fun getNamesXCHAddress(addressName: String): String {
        try {
            val url = "https://namesdaolookup.xchstorage.com/${addressName}.json"
            val response = service.getAddressDaoName(url)
            return response.asJsonObject.get(ADDRESS).asString
        } catch (ex: Exception) {
            Timber.d("Exception in getting address from NamesDaoLookUp: $ex")
        }
        return ""
    }

    companion object {
        private const val ADDRESS = "address"
    }

}