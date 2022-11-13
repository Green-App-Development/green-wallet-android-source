package com.android.greenapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.greenapp.domain.domainmodel.Address

/**
 * Created by bekjan on 06.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */


@Entity(tableName = "AddressEntity")
data class AddressEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "address_id")
    val address_id: String,
    @ColumnInfo(name = "address")
    val address: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name="updated_time")
    val updated_time:Long
) {

    fun toAddress() =
        Address(address_id,address = address, name = name, description = description,updated_time=updated_time)

}
