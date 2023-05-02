package com.green.wallet.data.interact

import com.example.common.tools.convertArrayStringToList
import com.green.wallet.data.local.NftCoinsDao
import com.green.wallet.data.local.NftInfoDao
import com.green.wallet.data.local.WalletDao
import com.green.wallet.data.local.relations.WalletWithNFTCoin
import com.green.wallet.data.local.relations.WalletWithNFTInfoRelation
import com.green.wallet.data.preference.PrefsManager
import com.green.wallet.domain.domainmodel.WalletWithNFTInfo
import com.green.wallet.domain.interact.NFTInteract
import com.green.wallet.domain.interact.PrefsInteract
import com.green.wallet.presentation.custom.AESEncryptor
import com.green.wallet.presentation.tools.VLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NFtInteractImpl @Inject constructor(
	private val walletDao: WalletDao,
	private val nftInfoDao: NftInfoDao,
	private val nftCoinDao: NftCoinsDao,
	private val encryptor: AESEncryptor,
	private val prefs: PrefsInteract
) : NFTInteract {


	override fun getHomeAddedWalletWithNFTTokensFlow(): Flow<List<WalletWithNFTInfo>> {
		return walletDao.getFLowOfWalletListWithNFTCoins().map {
			it.map {
				WalletWithNFTInfo(
					it.fingerPrint,
					it.address,
					it.nftInfos.map { it.toNFTInfo() }
				)
			}
		}
	}


}
