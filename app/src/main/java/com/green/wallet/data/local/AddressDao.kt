package com.green.wallet.data.local

import androidx.room.*
import com.green.wallet.data.local.entity.AddressEntity
import kotlinx.coroutines.flow.Flow

/**
 * Created by bekjan on 06.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */

@Dao
interface AddressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddressEntity(addressEntity: AddressEntity): Long

    @Delete
    suspend fun deleteAddressEntity(addressEntity: AddressEntity): Int

    @Update
    suspend fun updateAddressEntity(addressEntity: AddressEntity): Int

    @Query("SELECT * FROM AddressEntity ORDER BY updated_time DESC")
    fun getAddressFlowOfEntity(): Flow<List<AddressEntity>>

    @Query("SELECT * FROM AddressEntity WHERE name LIKE '%' || :name  || '%' ")
    fun searchAddressByName(name: String): Flow<List<AddressEntity>>

    @Query("SELECT * FROM AddressEntity WHERE address=:address")
    suspend fun checkIfAddressExist(address: String): List<AddressEntity>


}
