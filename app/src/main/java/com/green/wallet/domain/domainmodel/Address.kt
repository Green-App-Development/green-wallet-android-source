package com.green.wallet.domain.domainmodel


import android.os.Parcel
import android.os.Parcelable
import com.green.wallet.data.local.entity.AddressEntity
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Address  (
    val address_id: String,
    val address: String,
    val name: String,
    val description: String,
    val updated_time: Long
) : Parcelable  {

    fun toAddressEntity() = AddressEntity(address_id, address, name, description, updated_time)

}
