package com.green.wallet.domain.domainmodel

import com.green.wallet.presentation.tools.Status


data class Transaction(
	val transactionId: String,
	var amount: Double,
	val createdAtTime: Long,
	val confirmedAtHeight: Long,
	val status: Status,
	val networkType: String,
	val toDestHash: String,
	val fkAddress: String,
	val feeAmount: Double,
	var code:String,
	val nftCoinHash:String
)
