package com.green.wallet.domain.usecases.tibet

import javax.inject.Inject

class TibetSwapUseCases @Inject constructor(
	val calculateAmountOut: CalculateAmountOut,
	val pushOfferToTibet: PushingOfferToTibet,
	val pushOfferXCHCATToTibet: PushingOfferXCHCATToTibet,
	val pushOfferCATXCHToTibet: PushingOfferCATXCHToTibet,
	val addLiquidity: TibetAddLiquidity,
	val removeLiquidity: TibetRemoveLiquidity
)
