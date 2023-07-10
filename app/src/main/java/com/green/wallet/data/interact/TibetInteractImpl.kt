package com.green.wallet.data.interact

import com.green.wallet.data.local.TibetDao
import com.green.wallet.data.local.TokenDao
import com.green.wallet.data.network.TibetExchangeService
import com.green.wallet.domain.domainmodel.TibetLiquidity
import com.green.wallet.domain.domainmodel.TibetLiquidityExchange
import com.green.wallet.domain.domainmodel.TibetSwapResponse
import com.green.wallet.domain.interact.TibetInteract
import com.green.wallet.presentation.tools.Resource
import com.green.wallet.presentation.tools.VLog
import javax.inject.Inject

class TibetInteractImpl
@Inject constructor(
	private val tibetService: TibetExchangeService,
	private val tokenDao: TokenDao,
	private val tibetDao: TibetDao
) : TibetInteract {

	override suspend fun saveTokensPairID() {
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

	override suspend fun calculateAmountInAndOut(
		amountIn: Long,
		isXCH: Boolean,
		pairId: String
	): Resource<TibetSwapResponse> {
		return try {
			val res = tibetService.calculateAmountInOut(pairId, amountIn, isXCH)
			if (res.isSuccessful) {
				return Resource.success(res.body()!!.toTibetSwap())
			} else
				throw Exception(res.message())
		} catch (ex: Exception) {
			VLog.d("Exception caught in calculating amount out : ${ex.message}")
			Resource.error(ex)
		}
	}

	override suspend fun pushOfferToTibet(pair: String, offer: String,action:String): Resource<String> {
		try {
			val body = hashMapOf<String, Any>()
			body["offer"] = offer
			body["action"] = action
			body["total_donation_amount"] = 0
			body["donation_addresses"] = listOf<String>()
			body["donation_weights"] = listOf<String>()
			VLog.d("Making request to Tibet body : $body pair : $pair")
			val res = tibetService.pushingOfferToTibet(pair_id = pair, body = body)
			if (res.isSuccessful) {
				val success = res.body()!!.asJsonObject.get("success").asBoolean
				VLog.d("Result from pushing offer : $res and success : $success")
				if (success) {
					val offerId = res.body()!!.asJsonObject.get("offer_id").asString
					return Resource.success(offerId)
				}
				return Resource.error(Exception(res.message()))
			} else
				throw Exception(res.message())
		} catch (ex: Exception) {
			VLog.d("Pushing  offer to tibet exception : ${ex.message}")
			return Resource.error(ex)
		}
	}

	override suspend fun getTibetLiquidity(pairId: String): TibetLiquidityExchange? {
		return try {
			val res = tibetService.getTibetLiquidityByPairId(pairId)
			if (res.isSuccessful) {
				res.body()!!.toTibetLiquidity()
			} else
				throw Exception(res.message())
		} catch (ex: Exception) {
			VLog.d("Exception in getting tibet liquidity : ${ex.message}")
			null
		}
	}

	override suspend fun insertTibetLiquidity(tibetLiquidity: TibetLiquidity) {
		val tibetLiquidEntity = tibetLiquidity.toTibetLiquidityEntity()
		VLog.d("Inserting tibet liquidity entity : $tibetLiquidEntity")
		tibetDao.insertTibetLiquidityEntity(tibetLiquidEntity)
	}


}
