package com.green.wallet.data.local

import androidx.room.*
import androidx.room.OnConflictStrategy.Companion.IGNORE
import com.green.wallet.data.local.dto.ChiaWalletDTO
import com.green.wallet.data.local.entity.WalletEntity
import com.green.wallet.data.local.relations.WalletWithNFTInfoRelation
import com.green.wallet.presentation.tools.NetworkType
import kotlinx.coroutines.flow.Flow
import java.util.*


@Dao
interface WalletDao {

    @Insert(onConflict = IGNORE)
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
    fun getWalletListHomeIsAdded(): Flow<List<WalletEntity>>

    @Query("SELECT * FROM WalletEntity ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomWallet(): Optional<WalletEntity>

    @Query("SELECT * FROM WalletEntity WHERE networkType=:networkType AND (:fingerPrint IS NULL OR fingerPrint=:fingerPrint)")
    suspend fun getWalletListByNetworkTypeAndFingerPrint(
        networkType: String,
        fingerPrint: Long?
    ): List<WalletEntity>


    @Query("SELECT * FROM WalletEntity WHERE networkType=:networkType AND (:fingerPrint IS NULL OR fingerPrint=:fingerPrint)")
    fun getWalletListByNetworkTypeAndFingerPrintFlow(
        networkType: String,
        fingerPrint: Long?
    ): Flow<List<WalletEntity>>

    @Query("SELECT * FROM WalletEntity WHERE networkType=:networkType ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomWalletByNetworkType(networkType: NetworkType): List<WalletEntity>

    @Query("SELECT * FROM WalletEntity ORDER BY homeAdded ASC")
    suspend fun getAllWalletList(): List<WalletEntity>

    @Query("UPDATE WalletEntity SET balance=:new_balance WHERE fingerPrint=:fingerPrint")
    suspend fun updateWalletBalanceByFingerPrint(new_balance: Double, fingerPrint: Long)

    @Query("SELECT * FROM WalletEntity WHERE fingerPrint=:fingerPrint")
    suspend fun getWalletByFingerPrint(fingerPrint: Long): List<WalletEntity>

    @Query("SELECT DISTINCT networkType FROM WalletEntity ORDER BY networkType ASC")
    suspend fun getDistinctNetworkTypes(): List<String>

    @Update
    suspend fun updateWalletEntity(walletEntity: WalletEntity): Int


    @Query("UPDATE WalletEntity SET hashWithAmount=:hashWithAmount WHERE fingerPrint=:fingerPrint")
    suspend fun updateChiaNetworkHashTokenBalanceByFingerPrint(
        fingerPrint: Long,
        hashWithAmount: HashMap<String, Double>
    ): Int


    @Query("SELECT * FROM WalletEntity WHERE mnemonics=:encMnemonics AND networkType=:networkType")
    suspend fun checkIfEncMnemonicsExistInDB(
        encMnemonics: String,
        networkType: String
    ): Optional<WalletEntity>


    @Query("SELECT * FROM WalletEntity ORDER BY  homeAdded>0 DESC, homeAdded")
    fun getWalletListHomeAddedFirstThenRemainingFlow(): Flow<List<WalletEntity>>


    @Query("SELECT * FROM WalletEntity ORDER BY  homeAdded>0 DESC, homeAdded")
    suspend fun getWalletListHomeAddedFirstThenRemaining(): List<WalletEntity>


    @Query("UPDATE WalletEntity SET hashWithAmount=:hashWithAmount WHERE address=:address")
    suspend fun updateChiaNetworkHashTokenBalanceByAddress(
        address: String,
        hashWithAmount: HashMap<String, Double>
    ): Int


    @Query("UPDATE WalletEntity SET hashListImported=:hashListImportedNew WHERE address=:address")
    suspend fun updateChiaNetworkHashListImportedByAddress(
        address: String,
        hashListImportedNew: HashMap<String, List<String>>
    ): Int


    @Query("UPDATE WalletEntity SET balance=:new_balance WHERE address=:address")
    suspend fun updateWalletBalanceByAddress(new_balance: Double, address: String)

    @Query("SELECT * FROM WalletEntity WHERE address=:address")
    suspend fun getWalletByAddress(address: String): List<WalletEntity>

    @Query("DELETE FROM WalletEntity WHERE address=:address")
    suspend fun deleteWalletByAddress(address: String): Int

    @Query("SELECT * FROM WalletEntity WHERE (address LIKE '%' || 'xch'  || '%') OR  (address LIKE  '%' || 'txch'  || '%') ")
    suspend fun getWalletByNetworkTypeChia(): List<WalletEntity>

    @Query("UPDATE WalletEntity SET puzzle_hashes=:puzzle_hashes WHERE address=:address")
    suspend fun updateWalletMainPuzzleHashesByAddress(
        puzzle_hashes: List<String>,
        address: String
    ): Int

    @Query("UPDATE WalletEntity SET hashListImported=:hashListImportedNew WHERE address=:address")
    suspend fun updateWalletTokenPuzzleHashesByAddress(
        hashListImportedNew: HashMap<String, List<String>>,
        address: String
    ): Int

    @Query("UPDATE WalletEntity SET observer_hash=:observer,non_observer_hash=:nonObserver WHERE address=:address")
    suspend fun updateObserverHashCount(address: String, observer: Int, nonObserver: Int): Int

    @Query("UPDATE WalletEntity SET tokens_start_height=:tokenStartHeight WHERE address=:address")
    suspend fun updateTokenStartHeightByAddress(
        tokenStartHeight: HashMap<String, Long>,
        address: String
    )

    @Transaction
    @Query("SELECT fingerPrint,mnemonics,observer_hash,non_observer_hash,address FROM WalletEntity WHERE homeAdded>0 ORDER BY homeAdded ASC")
    fun getFLowOfWalletListWithNFTCoins(): Flow<List<WalletWithNFTInfoRelation>>

    @Query("SELECT fingerPrint,address FROM WalletEntity")
    suspend fun getChiaWalletList(): List<ChiaWalletDTO>

    @Query("UPDATE WalletEntity SET mnemonics=:encrypted,encrypt_stage=:stage WHERE address=:address")
    suspend fun updateMnemonicsAndEncStage(encrypted: String, stage: Int, address: String)

    @Query("SELECT * FROM WalletEntity WHERE homeAdded>0 ORDER BY homeAdded ASC LIMIT 1")
    suspend fun getHomeFirstWallet(): List<WalletEntity>

}
