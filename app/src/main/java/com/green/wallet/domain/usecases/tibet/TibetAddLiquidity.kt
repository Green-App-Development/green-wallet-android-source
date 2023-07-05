package com.green.wallet.domain.usecases.tibet

import com.green.wallet.domain.domainmodel.TibetLiquidity
import com.green.wallet.domain.interact.ExchangeInteract
import com.green.wallet.domain.interact.SpentCoinsInteract
import com.green.wallet.domain.interact.TibetInteract
import com.green.wallet.presentation.tools.Resource
import javax.inject.Inject

class TibetAddLiquidity @Inject constructor(
    private val tibetInteract: TibetInteract,
    private val spentCoinsInteract: SpentCoinsInteract,
    private val exchangeInteract: ExchangeInteract
) {

    suspend operator fun invoke(
        offer: String,
        pairId: String,
        xchCoins: String,
        tokenCoins: String,
        address: String,
        tibetLiquidity: TibetLiquidity
    ): Resource<String> {
        val res = tibetInteract.pushOfferToTibet(pairId, offer)
        if (res.state == Resource.State.SUCCESS) {
            val offerId = res.data!!
            tibetLiquidity.offer_id = offerId

        }
        return res
    }

}
