package com.green.wallet.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.green.wallet.domain.domainmodel.Address

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
	@ColumnInfo(name = "updated_time")
	val updated_time: Long,
) {

	fun toAddress() =
		Address(
			address_id,
			address = address,
			name = name,
			description = description,
			updated_time = updated_time
		)


}
