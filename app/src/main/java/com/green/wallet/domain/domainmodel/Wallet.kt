package com.green.wallet.domain.domainmodel

import android.os.Parcelable
import com.green.wallet.data.local.entity.WalletEntity
import kotlinx.android.parcel.Parcelize


@Parcelize
data class Wallet(
    val fingerPrint: Long,
    val puzzle_hashes: List<String>,
    val address: String,
    val mnemonics: List<String>,
    val networkType: String,
    var home_id_added: Long,
    var balance: Double,
    var savedTime: Long,
    val observerHash: Int,
    val nonObserverHash: Int,
    val hashListImported: HashMap<String, List<String>> = hashMapOf()
) : Parcelable {

    var balanceInUSD: Double = 0.0

    fun toWalletEntity(encMnemonics: String, savedTime: Long) = WalletEntity(
        fingerPrint,
        "",
        puzzle_hashes,
        address,
        encMnemonics,
        networkType,
        home_id_added,
        balance,
        savedTime = savedTime,
        hashListImported = hashListImported,
        observer_hash = observerHash,
        non_observer_hash = nonObserverHash,
        encryptedStage = 1
    )


}
