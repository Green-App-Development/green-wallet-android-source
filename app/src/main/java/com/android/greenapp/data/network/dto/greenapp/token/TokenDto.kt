package com.android.greenapp.data.network.dto.greenapp.token

import com.android.greenapp.data.local.entity.TokenEntity


data class TokenDto(
	val name: String,
	val code: String,
	val hash: String,
	val logo_url: String?,
	val default_tail: Int
) {
	fun toTokenEntity() = TokenEntity(
		code,
		name,
		hash,
		logo_url ?: "",
		default_tail = default_tail,
		enabled = true
	)
}
