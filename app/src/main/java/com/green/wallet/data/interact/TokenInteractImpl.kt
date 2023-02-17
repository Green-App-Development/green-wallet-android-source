package com.green.wallet.data.interact

import com.green.wallet.data.local.TokenDao
import com.green.wallet.data.local.WalletDao
import com.green.wallet.domain.domainmodel.Token
import com.green.wallet.domain.interact.TokenInteract
import com.green.wallet.presentation.custom.ServerMaintenanceExceptions
import com.green.wallet.presentation.tools.Resource
import com.green.wallet.presentation.tools.VLog
import javax.inject.Inject

/**
 * Created by bekjan on 12.07.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class TokenInteractImpl @Inject constructor(
	private val tokenDao: TokenDao,
	private val walletDoa: WalletDao
) : TokenInteract {


	override suspend fun getTokenListAndSearchForWallet(
		fingerPrint: Long,
		nameCode: String?
	): Resource<List<Token>> {

		val walletEntity = walletDoa.getWalletByFingerPrint(fingerPrint)[0]
		val tokens = tokenDao.getTokenListAndSearch(nameCode)
			.filter { !arrayOf("XCH", "XCC").contains(it.code) }
		VLog.d("Token List : $tokens and NameCode : $nameCode")
		if (tokens.isEmpty() && nameCode == null) {
			return Resource.error(ServerMaintenanceExceptions())
		}

		return Resource.success(tokens.map { tokenEntity ->
			val imported = walletEntity.hashListImported.containsKey(tokenEntity.hash)
			tokenEntity.toToken(
				imported,
				if (!walletEntity.hashListImported.containsKey(tokenEntity.hash)) 0 else tokenEntity.default_tail
			)
		}.filter { token: Token ->  !arrayOf("XCH", "XCC").contains(token.code) })
	}

	override suspend fun getTokenPriceByCode(code: String): Double {
		val tokenOpt = tokenDao.getTokenByCode(code)
		if (tokenOpt.isPresent)
			return tokenOpt.get().price
		return 0.0
	}

	override suspend fun getTokenListDefaultOnMainScreen(): List<Token> =
		tokenDao.getTokensDefaultOnScreen().map { it.toToken(imported = false, 1) }


}
