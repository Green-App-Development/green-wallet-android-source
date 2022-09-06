package com.android.greenapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.greenapp.domain.entity.Address

/**
 * Created by bekjan on 06.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */


@Entity(tableName = "AddressEntity")
data class AddressEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "address")
    val address: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "description")
    val description: String
) {

    fun toAddress() =
        Address(address = address, name = name, description = description)

}
