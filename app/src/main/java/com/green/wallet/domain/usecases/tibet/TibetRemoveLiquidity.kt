package com.green.wallet.domain.usecases.tibet

import com.green.wallet.domain.domainmodel.TibetLiquidity
import com.green.wallet.domain.interact.ExchangeInteract
import com.green.wallet.domain.interact.SpentCoinsInteract
import com.green.wallet.domain.interact.TibetInteract
import javax.inject.Inject

class TibetRemoveLiquidity @Inject constructor(
    private val tibetInteract: TibetInteract,
    private val spentCoinsInteract: SpentCoinsInteract,
    private val exchangeInteract: ExchangeInteract
) {

    suspend operator fun invoke(
        offer: String,
        pairId: String,
        liquidityCoins:String,
        address: String,
        tibetLiquidity: TibetLiquidity
    ) {

    }

}
