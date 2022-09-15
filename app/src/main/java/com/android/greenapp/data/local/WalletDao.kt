package com.android.greenapp.data.local

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.android.greenapp.data.local.entity.WalletEntity
import com.android.greenapp.presentation.tools.NetworkType
import kotlinx.coroutines.flow.Flow
import java.util.*


/**
 * Created by bekjan on 09.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */


@Dao
interface WalletDao {

    @Insert(onConflict = REPLACE)
    suspend fun insertWallet(walletEntity: WalletEntity): Long

    @Delete
    suspend fun deleteWallet(walletEntity: WalletEntity): Int

    @Query("DELETE FROM WalletEntity WHERE fingerPrint=:fingerPrint")
    suspend fun deleteWalletByFingerPrint(fingerPrint: Long): Int

    @Query("SELECT * FROM WalletEntity ORDER BY homeAdded ASC")
    fun getFlowOfAllWalletList(): Flow<List<WalletEntity>>

    @Query("UPDATE WalletEntity SET homeAdded=:curMillis WHERE fingerPrint=:fingerPrint")
    suspend fun updateHomeIsAdded(curMillis: Long, fingerPrint: Long): Int

    @Query("SELECT * FROM WalletEntity WHERE homeAdded>0  ORDER BY homeAdded ASC")
    suspend fun getWalletListHomeIsAdded(): List<WalletEntity>

    @Query("SELECT * FROM WalletEntity ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomWallet(): Optional<WalletEntity>

    @Query("SELECT * FROM WalletEntity WHERE networkType=:networkType AND (:fingerPrint IS NULL OR fingerPrint=:fingerPrint)")
    suspend fun getWalletListByNetworkTypeAndFingerPrint(
        networkType: String,
        fingerPrint: Long?
    ): List<WalletEntity>

    @Query("SELECT * FROM WalletEntity WHERE networkType=:networkType ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomWalletByNetworkType(networkType: NetworkType): List<WalletEntity>

    @Query("SELECT * FROM WalletEntity ORDER BY homeAdded ASC")
    suspend fun getAllWalletList(): List<WalletEntity>

    @Query("UPDATE WalletEntity SET balance=:new_balance WHERE fingerPrint=:fingerPrint")
    suspend fun updateWalletBalance(new_balance: Double, fingerPrint: Long)

    @Query("SELECT * FROM WalletEntity WHERE fingerPrint=:fingerPrint")
    suspend fun getWalletByFingerPrint(fingerPrint: Long): List<WalletEntity>

    @Query("SELECT DISTINCT networkType FROM WalletEntity ORDER BY networkType ASC")
    suspend fun getDistinctNetworkTypes(): List<String>

    @Update
    suspend fun updateWalletEntity(walletEntity: WalletEntity): Int


    @Query("UPDATE WalletEntity SET hashWithAmount=:hashWithAmount WHERE fingerPrint=:fingerPrint")
    suspend fun updateChiaNetworkHashTokenBalance(
        fingerPrint: Long,
        hashWithAmount: HashMap<String, Double>
    ): Int


    @Query("UPDATE WalletEntity SET hashListImported=:hashListImportedNew WHERE fingerPrint=:fingerPrint")
    suspend fun updateChiaNetworkHashListImported(
        fingerPrint: Long,
        hashListImportedNew: MutableSet<String>
    ): Int


    @Query("SELECT * FROM WalletEntity WHERE mnemonics=:encMnemonics AND networkType=:networkType")
    suspend fun checkIfEncMnemonicsExistInDB(
        encMnemonics: String,
        networkType: String
    ): Optional<WalletEntity>

    @Query("SELECT * FROM WalletEntity WHERE homeAdded==0")
    suspend fun getWalletListHomeIsNotAdded(): List<WalletEntity>

    @Query("SELECT * FROM WalletEntity ORDER BY  homeAdded>0 DESC, homeAdded")
    suspend fun getWalletListHomeAddedFirstThenRemaining(): List<WalletEntity>


}