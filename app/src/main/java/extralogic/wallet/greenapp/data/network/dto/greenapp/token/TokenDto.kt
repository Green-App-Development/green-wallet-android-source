package extralogic.wallet.greenapp.data.network.dto.greenapp.token

import extralogic.wallet.greenapp.data.local.entity.TokenEntity


data class TokenDto(
	val name: String,
	val code: String,
	val hash: String,
	val logo_url: String?,
	val default_tail: Int,
	val price: String?
) {
	fun toTokenEntity() = TokenEntity(
		code,
		name,
		hash,
		logo_url ?: "",
		default_tail = default_tail,
		enabled = true,
		price = price?.toDoubleOrNull() ?: 0.0
	)
}
