package com.green.wallet.data.interact

import com.green.wallet.data.network.DexieService
import com.green.wallet.domain.domainmodel.DexieFees
import com.green.wallet.domain.interact.DexieInteract
import com.green.wallet.presentation.tools.VLog
import java.lang.IllegalArgumentException
import javax.inject.Inject


class DexieInteractImpl @Inject constructor(private val dexieService: DexieService) :
    DexieInteract {

    override suspend fun getDexieMinFee(): DexieFees {
        return try {
            val res = dexieService.getDexieStatus()
            if (res.isSuccessful && res.body() != null) {
                val fees =
                    res.body()!!.asJsonObject.get("status").asJsonObject.get("fees").asJsonObject
                val min = fees.get("min").asDouble
                val recommended = fees.get("recommended").asDouble
                DexieFees(min = min, recommended = recommended)
            } else {
                throw IllegalArgumentException(res.message())
            }
        } catch (ex: Exception) {
            VLog.d("Exception in getting dexie status $ex")
            DexieFees(min = 0.00028, 0.00028)
        }
    }

}