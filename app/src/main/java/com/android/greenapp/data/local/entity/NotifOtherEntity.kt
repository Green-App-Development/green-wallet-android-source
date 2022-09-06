package com.android.greenapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "NotificationOther")
data class NotifOtherEntity(
    @PrimaryKey(autoGenerate = false)
    @SerializedName("guid")
    val guid: String,
    @SerializedName("created_at_time")
    val created_at_time: Long,
    @SerializedName("message")
    val message: String
)