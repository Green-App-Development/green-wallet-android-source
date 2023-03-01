package com.green.wallet.domain.interact

import com.green.wallet.domain.domainmodel.Address
import kotlinx.coroutines.flow.Flow


interface AddressInteract {


    suspend fun insertAddressEntity(address: Address): Long

    suspend fun deleteAddressEntity(addressEntity: Address): Int

    suspend fun updateAddressEntity(addressEntity: Address): Int

    suspend fun searchAddressByName(name: String): Flow<List<Address>>


    fun getAllSavedAddressList(): Flow<List<Address>>

    suspend fun checkIfAddressAlreadyExist(address:String):List<Address>

}
