package com.green.wallet.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.green.wallet.domain.domainmodel.Token


@Entity(tableName = "TokenEntity")
data class TokenEntity(
	@PrimaryKey(autoGenerate = false)
	val code: String,
	val name: String,
	val hash: String,
	val logo_url: String,
	var price: Double = 0.0,
	val default_tail: Int,
	var enabled: Boolean,
	@ColumnInfo(name = "pair_id", defaultValue = "")
	var pair_id: String = ""
) {

	fun toToken(imported: Boolean, default_tail: Int, pair_id: String = "") =
		Token(name, code, hash, logo_url, imported, default_tail, pairID = pair_id)

}
