package extralogic.wallet.greenapp.domain.domainmodel


data class Token(
    val name: String,
    val code: String,
    val hash: String,
    val logo_url: String,
    var imported: Boolean,
	val defaultTail:Int
) {
    val price: Double = 0.0
}
