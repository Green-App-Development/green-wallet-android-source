package com.green.wallet.domain.domainmodel

import android.os.Parcel
import android.os.Parcelable
import com.green.wallet.data.local.entity.AddressEntity


data class Address(
    val address_id: String,
    val address: String,
    val name: String,
    val description: String,
    val updated_time: Long
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readLong()
    )

    fun toAddressEntity() = AddressEntity(address_id, address, name, description, updated_time)

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(p0: Parcel?, p1: Int) {

    }

    companion object CREATOR : Parcelable.Creator<Address> {
        override fun createFromParcel(parcel: Parcel): Address {
            return Address(parcel)
        }

        override fun newArray(size: Int): Array<Address?> {
            return arrayOfNulls(size)
        }
    }

}
