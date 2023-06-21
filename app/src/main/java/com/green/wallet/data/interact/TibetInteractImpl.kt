package com.green.wallet.data.interact

import com.green.wallet.data.local.TokenDao
import com.green.wallet.data.network.TibetExchangeService
import com.green.wallet.domain.interact.TibetInteract
import com.green.wallet.presentation.tools.VLog
import javax.inject.Inject

class TibetInteractImpl
@Inject constructor(
	private val tibetService: TibetExchangeService,
	private val tokenDao: TokenDao
) : TibetInteract {

	override suspend fun getTokensWithPairID() {
		try {

			val res = tibetService.getTokensWithPairID()
			if (res.isSuccessful) {
				val tokenList = res.body()!!
				for (i in 0 until tokenList.size()) {
					val tokenJson = tokenList[i]
					val assetId = tokenJson.asJsonObject.get("asset_id").asString
					val pairId = tokenJson.asJsonObject.get("pair_id").asString
					VLog.d("Asset ID : $assetId  PairID : $pairId")
					tokenDao.updateTokenEntityPairIDByHash(pair_id = pairId, hash = assetId)
				}
			} else {
				VLog.d("Request is no success in getting tokens with pair id : ${res.message()}")
			}
		} catch (ex: Exception) {
			VLog.d("Exception occurred while getting tokens with pair : ${ex.message}")
		}
	}


}
