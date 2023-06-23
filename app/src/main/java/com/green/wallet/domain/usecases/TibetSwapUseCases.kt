package com.green.wallet.domain.usecases

import javax.inject.Inject

class TibetSwapUseCases @Inject constructor(
	val calculateAmountOut: CalculateAmountOut
)
