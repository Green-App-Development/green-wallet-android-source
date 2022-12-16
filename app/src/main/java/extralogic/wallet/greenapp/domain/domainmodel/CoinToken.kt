package extralogic.wallet.greenapp.domain.domainmodel


data class CoinToken(
    val name: String,
    val belowName: String,
    var home_is_added: Boolean,
    var detailLayoutVisible: Boolean
)