package com.android.greenapp.data.interact

import com.android.greenapp.data.local.TokenDao
import com.android.greenapp.data.local.WalletDao
import com.android.greenapp.domain.domainmodel.Token
import com.android.greenapp.domain.interact.TokenInteract
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
	): List<Token> {
		val walletEntity = walletDoa.getWalletByFingerPrint(fingerPrint)[0]

		return tokenDao.getTokenListAndSearch(nameCode).map {
			val imported = walletEntity.hashListImported.containsKey(it.hash)
			it.toToken(
				imported,
				if (!walletEntity.hashListImported.containsKey(it.hash)) 0 else it.default_tail
			)
		}.filter { !arrayOf("XCH", "XCC").contains(it.code) }
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
