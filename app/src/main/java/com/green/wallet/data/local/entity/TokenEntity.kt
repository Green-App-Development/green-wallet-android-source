package com.green.wallet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.green.wallet.domain.domainmodel.Token

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
    var price: Double = 0.0,
    val default_tail: Int,
    var enabled: Boolean
) {

	fun toToken(imported: Boolean,default_tail:Int) = Token(name, code, hash, logo_url, imported, default_tail)

}
