package com.android.greenapp.domain.interact

import com.android.greenapp.domain.entity.Address
import kotlinx.coroutines.flow.Flow
import java.util.*

/**
 * Created by bekjan on 06.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */
interface AddressInteract {


    suspend fun insertAddressEntity(address: Address): Long

    suspend fun deleteAddressEntity(addressEntity: Address): Int

    suspend fun updateAddressEntity(addressEntity: Address): Int

    suspend fun searchAddressByName(name: String): Flow<List<Address>>


    fun getAllSavedAddressList(): Flow<List<Address>>

    suspend fun checkIfAddressAlreadyExist(address:String):List<Address>

}