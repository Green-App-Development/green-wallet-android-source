package extralogic.wallet.greenapp.data.network.dto.spendbundle

data class SpendBundle(
    val aggregated_signature: String,
    val coin_spends: List<CoinSpend>
)
