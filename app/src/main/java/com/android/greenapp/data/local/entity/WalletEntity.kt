package com.android.greenapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.greenapp.domain.entity.Wallet
import java.util.*

/**
 * Created by bekjan on 09.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */

@Entity(tableName = "WalletEntity")
data class WalletEntity(
    @ColumnInfo(name = "fingerPrint")
    val fingerPrint: Long,
    @ColumnInfo(name = "privateKey")
    val privateKey: String,
    @ColumnInfo(name = "sk")
    val sk: String,
	@PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "address")
    val address: String,
    @ColumnInfo(name = "mnemonics")
    val encMnemonics:String,
    @ColumnInfo(name = "networkType")
    val networkType: String,
    @ColumnInfo(name = "homeAdded")
    val home_is_added: Long,
    @ColumnInfo(name = "balance")
    val balance: Double,
	//asset_id,cat_wrapped_puzzle_hash
    @ColumnInfo(name = "hashListImported")
    val hashListImported: HashMap<String,String> = hashMapOf(),
    @ColumnInfo(name = "hashWithAmount")
    var hashWithAmount: HashMap<String, Double> = hashMapOf(),
    @ColumnInfo(name="savedTime")
    var savedTime:Long
) {

    fun toWallet(decMnemonics:List<String>) =
        Wallet(
            fingerPrint,
            privateKey,
            sk,
            address,
            decMnemonics,
            networkType,
            home_is_added,
            balance,
            savedTime
        )

}
