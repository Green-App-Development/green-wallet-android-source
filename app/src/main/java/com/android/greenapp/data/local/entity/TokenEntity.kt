package com.android.greenapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.greenapp.domain.entity.Token

/**
 * Created by bekjan on 12.07.2022.
 * email: bekjan.omirzak98@gmail.com
 */

@Entity(tableName = "TokenEntity")
data class TokenEntity(
    @PrimaryKey(autoGenerate = false)
    val code: String,
    val name: String,
    val hash: String,
    val logo_url: String,
    val price: Double = 0.0
) {

    fun toToken(imported: Boolean) = Token(name, code, hash, logo_url, imported)

}