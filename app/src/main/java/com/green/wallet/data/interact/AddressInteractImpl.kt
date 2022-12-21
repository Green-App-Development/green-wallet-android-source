package com.green.wallet.data.interact

import com.green.wallet.data.local.AddressDao
import com.green.wallet.domain.domainmodel.Address
import com.green.wallet.domain.interact.AddressInteract
import com.example.common.tools.VLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Created by bekjan on 06.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */
class AddressInteractImpl @Inject constructor(private val addressDao: AddressDao) :
    AddressInteract {

    override suspend fun insertAddressEntity(address: Address): Long {
        VLog.d("Saving Address Entity : $address")
        return addressDao.insertAddressEntity(addressEntity = address.toAddressEntity())
    }

    override suspend fun deleteAddressEntity(addressEntity: Address): Int =
        addressDao.deleteAddressEntity(addressEntity = addressEntity.toAddressEntity())

    override suspend fun updateAddressEntity(addressEntity: Address): Int =
        addressDao.updateAddressEntity(addressEntity = addressEntity.toAddressEntity())

    override suspend fun searchAddressByName(name: String): Flow<List<Address>> {
        return addressDao.searchAddressByName(name)
            .map { listAddEnt -> listAddEnt.map { address -> address.toAddress() } }
    }

    override fun getAllSavedAddressList(): Flow<List<Address>> =
        addressDao.getAddressFlowOfEntity().map { listAddressEntity ->
            listAddressEntity.map { it.toAddress() }
        }

    override suspend fun checkIfAddressAlreadyExist(address: String) =
        addressDao.checkIfAddressExist(address).map { it.toAddress() }


}
