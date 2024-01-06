package com.green.wallet.presentation.main.transaction.btmSpeedy

import androidx.lifecycle.viewModelScope
import com.green.wallet.domain.domainmodel.Transaction
import com.green.wallet.domain.interact.NFTInteract
import com.green.wallet.domain.interact.TokenInteract
import com.green.wallet.presentation.main.dapp.trade.models.CatToken
import com.green.wallet.presentation.main.dapp.trade.models.NftToken
import com.green.wallet.presentation.tools.VLog
import com.greenwallet.core.base.BaseViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class SpeedyDialogViewModel @Inject constructor(
    private val tokenInteract: TokenInteract,
    private val nftInteract: NFTInteract
) : BaseViewModel<SpeedyTokenState, SpeedyTokenEvent>(SpeedyTokenState()) {

    private lateinit var transaction: Transaction


    fun setCurTransaction(tran: Transaction?) {
        VLog.d("Setting up transaction property : $tran")
        tran?.let {
            this.transaction = tran
            viewModelScope.launch {
                getInfoAboutTransaction()
            }
        }
    }

    private suspend fun getInfoAboutTransaction() {
        if (transaction.code == "NFT") {
            val nftInfo = nftInteract.getNftINFOByHash(transaction.nftCoinHash)
            VLog.d("NftInfo Entity from DB : $nftInfo")
            _viewState.update {
                it.copy(
                    token = NftToken(
                        collection = nftInfo.collection,
                        nftId = nftInfo.nft_id,
                        imgUrl = nftInfo.data_url
                    )
                )
            }
        } else {
            val token = tokenInteract.getTokenByCode(transaction.code) ?: return
            _viewState.update {
                it.copy(
                    token = CatToken(
                        transaction.code,
                        assetID = token.hash,
                        transaction.amount
                    )
                )
            }
        }
    }

}