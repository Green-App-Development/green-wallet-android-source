package com.green.wallet.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by bekjan on 05.10.2022.
 * email: bekjan.omirzak98@gmail.com
 */
@Entity(tableName = "user_post")
data class UserPostEntity(

	@PrimaryKey(autoGenerate = true)
	@ColumnInfo(name = "id")
	val id: Long = 0L,

	@ColumnInfo(name = "text")
	var text: String,
)
