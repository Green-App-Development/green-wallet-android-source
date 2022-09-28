package com.android.greenapp.domain.entity

import android.os.Parcelable
import com.android.greenapp.data.local.entity.WalletEntity
import kotlinx.android.parcel.Parcelize

/**
 * Created by bekjan on 09.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */

@Parcelize
data class Wallet(
	val fingerPrint: Long,
	val privateKey: String,
	val sk: String,
	val address: String,
	val mnemonics: List<String>,
	val networkType: String,
	var home_id_added: Long,
	var balance: Double,
	var savedTime: Long
) : Parcelable {

	var balanceInUSD: Double = 0.0

	fun toWalletEntity(encMnemonics: String) = WalletEntity(
		fingerPrint,
		privateKey,
		sk,
		address,
		encMnemonics,
		networkType,
		home_id_added,
		balance,
		savedTime = savedTime,
		hashListImported = hashListImported
	)

	val hashListImported= hashMapOf<String,String>()

}
