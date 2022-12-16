package extralogic.wallet.greenapp.domain.interact

import extralogic.wallet.greenapp.domain.domainmodel.Address
import kotlinx.coroutines.flow.Flow

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