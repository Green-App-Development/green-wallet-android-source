package extralogic.wallet.greenapp.data.network.dto.greenapp.token



data class TokenResponse(
    val version: String,
    val list: List<TokenDto>
)