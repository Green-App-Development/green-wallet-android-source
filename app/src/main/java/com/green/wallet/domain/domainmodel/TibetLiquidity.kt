package com.green.wallet.domain.domainmodel

import com.green.wallet.data.local.entity.TibetLiquidityEntity
import com.green.wallet.presentation.tools.OrderStatus

data class TibetLiquidity(
    var offer_id: String,
    val xchAmount: Double,
    val catAmount: Double,
    val catToken: String,
    val liquidityAmount: Double,
    val liquidityToken: String,
    val fee: Double,
    var time_created: Long,
    val status: OrderStatus = OrderStatus.InProgress,
    val height: Int = 0,
    val addLiquidity: Boolean,
	val fk_address:String
) {

    fun toTibetLiquidityEntity() = TibetLiquidityEntity(
        offer_id,
        xchAmount,
        catAmount,
        catToken,
        liquidityAmount,
        liquidityToken,
        fee,
        time_created,
        status,
        height,
        addLiquidity,
		fk_address
    )


}
