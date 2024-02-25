package com.green.wallet.domain.domainmodel

import com.green.wallet.data.local.entity.OfferTransactionEntity
import com.green.wallet.presentation.main.dapp.trade.models.CatToken
import com.green.wallet.presentation.main.dapp.trade.models.Token
import com.green.wallet.presentation.tools.Status


data class OfferTransaction(
    val transId: String = "",
    val createAtTime: Long = 0L,
    val requested: List<Token> = listOf(
        CatToken("XCH", "", 0.00003),
        CatToken("XCH", "", 0.00003),
    ),
    val offered: List<Token> = listOf(
        CatToken("XCH", "", 0.00003),
    ),
    val source: String = "",
    val hashTransaction: String = " ",
    val fee: Double = 0.0,
    val height: Long = 0L,
    val acceptOffer: Boolean = true
) : Transaction {


    fun toOfferTransactionEntity(addressFk: String) = OfferTransactionEntity(
        tranId = transId,
        createdTime = createAtTime,
        requested = requested,
        offered = offered,
        source = source,
        addressFk = addressFk,
        acceptOffer = acceptOffer,
        hashTransaction = hashTransaction,
        fee = fee,
        height = height,
        status = Status.InProgress
    )

}