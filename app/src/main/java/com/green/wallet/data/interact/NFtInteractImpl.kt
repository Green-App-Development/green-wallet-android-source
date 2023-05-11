package com.green.wallet.data.interact

import com.green.wallet.data.local.NftCoinsDao
import com.green.wallet.data.local.NftInfoDao
import com.green.wallet.data.local.WalletDao
import com.green.wallet.domain.domainmodel.NFTCoin
import com.green.wallet.domain.domainmodel.NFTInfo
import com.green.wallet.domain.domainmodel.WalletWithNFTInfo
import com.green.wallet.domain.interact.NFTInteract
import com.green.wallet.domain.interact.PrefsInteract
import com.green.wallet.presentation.custom.AESEncryptor
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
					it.nftInfos.filter { !it.spent }.map { it.toNFTInfo() }
				)
			}
		}
	}

	override suspend fun getNFTCoinByHash(coin_hash: String): NFTCoin {
		val nftCoin = nftCoinDao.getNFTCoinByParentCoinInfo(coin_hash)
		return nftCoin.get().toNftCoin()
	}

	override suspend fun getNftINFOByHash(nft_coin_hash: String): NFTInfo {
		val nftInfo = nftInfoDao.getNftInfoEntityByNftCoinHash(nft_coin_hash)
		return nftInfo.get().toNFTInfo()
	}


}
