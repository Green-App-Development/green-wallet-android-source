package com.green.wallet.data.interact

import androidx.work.impl.model.Preference
import com.example.common.tools.convertArrayStringToList
import com.green.wallet.data.local.NftCoinsDao
import com.green.wallet.data.local.NftInfoDao
import com.green.wallet.data.local.WalletDao
import com.green.wallet.data.local.relations.WalletWithNFT
import com.green.wallet.data.preference.PrefsManager
import com.green.wallet.domain.domainmodel.NFTCoin
import com.green.wallet.domain.domainmodel.NFTInfo
import com.green.wallet.domain.domainmodel.WalletWithNFTAndCoins
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


	override fun getHomeAddedWalletWithNFTTokensFlow(): Flow<List<WalletWithNFTAndCoins>> {
		return walletDao.getFLowOfWalletListWithNFTCoins().map {
			it.map { mapWalletNFTCoins(it) }
		}
	}

	private suspend fun mapWalletNFTCoins(it: WalletWithNFT): WalletWithNFTAndCoins {
		VLog.d("MappingWalletNFTCoins : $it with their nftInfo")
		val nftMap = hashMapOf<NFTCoin, NFTInfo>()
		for (nftCoin in it.nftCoins) {
			val optNftInfo = nftInfoDao.getNftInfoEntityByNftCoinHash(nftCoin.coin_info)
			if (optNftInfo.isPresent) {
				nftMap[nftCoin.toNftCoin()] = optNftInfo.get().toNFTInfo()
			}
		}
		val key = encryptor.getAESKey(prefs.getSettingString(PrefsManager.PASSCODE, ""))
		val mnemonics = convertArrayStringToList(encryptor.decrypt(it.mnemonics, key!!))
		return WalletWithNFTAndCoins(
			mnemonics,
			it.fingerPrint,
			it.address,
			it.observer_hash,
			it.non_observer_hash,
			nftMap
		)
	}


}
