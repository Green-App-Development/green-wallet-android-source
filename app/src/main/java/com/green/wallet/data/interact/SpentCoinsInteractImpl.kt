package com.green.wallet.data.interact

import com.green.wallet.data.local.SpentCoinsDao
import com.green.wallet.data.local.entity.SpentCoinsEntity
import com.green.wallet.domain.domainmodel.SpentCoin
import com.green.wallet.domain.interact.SpentCoinsInteract
import com.green.wallet.presentation.custom.getShortNetworkType
import com.green.wallet.presentation.tools.VLog
import com.example.common.tools.getTokenPrecisionByCode
import com.green.wallet.data.local.WalletDao
import kotlinx.coroutines.flow.*
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

class SpentCoinsInteractImpl @Inject constructor(
    private val spentCoinsDao: SpentCoinsDao,
    private val walletDao: WalletDao
) : SpentCoinsInteract {

    override suspend fun insertSpentCoinsJson(
        spentCoinsJson: String,
        time_created: Long,
        code: String,
        fk_address: String
    ) {
        try {
            val spentCoinsJsonArray = JSONArray(spentCoinsJson)
            for (i in 0 until spentCoinsJsonArray.length()) {
                val coinJson = JSONObject(spentCoinsJsonArray[i].toString())
                val amount = coinJson.getLong("amount")
                val parent_coin_info = coinJson.getString("parent_coin_info")
                val puzzle_hash = coinJson.getString("puzzle_hash")
                val division = getTokenPrecisionByCode(code)
                val coinEntity = SpentCoinsEntity(
                    parent_coin_info,
                    puzzle_hash,
                    amount / division,
                    fk_address,
                    code,
                    time_created
                )
                VLog.d("InsertingCoinEntity on coinInteractImpl class after pushing : $coinEntity")
                spentCoinsDao.insertSpentCoins(coinEntity)
            }
        } catch (ex: Exception) {
            VLog.d("Exception in inserting spentCoins : ${ex.message}")
        }
    }

    override suspend fun getSpentCoinsToPushTrans(
        networkType: String,
        address: String,
        tokenCode: String
    ): List<SpentCoin> {
        val division = getTokenPrecisionByCode(tokenCode)
        val spentCoins = spentCoinsDao.getSpentCoinsByAddressCode(address, tokenCode)
            .map { it.toSpentCoin((it.amount * division).toLong()) }.toMutableList()
        val feeTokenCode = getShortNetworkType(networkType)
        if (tokenCode == feeTokenCode) {
            return spentCoins
        }
        val divFee = getTokenPrecisionByCode(feeTokenCode)
        val feeCoins = spentCoinsDao.getSpentCoinsByAddressCode(address, feeTokenCode).map {
            it.toSpentCoin((it.amount * divFee).toLong())
        }.toList()
        spentCoins.addAll(feeCoins)
        return spentCoins.toList()
    }

    override fun getSumSpentCoinsForSpendableBalance(
        networkType: String,
        address: String,
        tokenCode: String
    ): Flow<DoubleArray> {
        val tokenAmountFlow =
            spentCoinsDao.getSpentCoinsByAddressCodeFlow(address, tokenCode)
                .map { list -> list.map { it.amount }.sum() }
        val feeTokenCode = getShortNetworkType(networkType)
        if (tokenCode == feeTokenCode) {
            return tokenAmountFlow.map {
                doubleArrayOf(it, it)
            }
        }
        val feeAmountFlow =
            spentCoinsDao.getSpentCoinsByAddressCodeFlow(address, feeTokenCode)
                .map { list -> list.map { it.amount }.sum() }
        VLog.d("TokenAmount : $tokenAmountFlow and FeeAmount : $feeAmountFlow")

        return tokenAmountFlow.combine(feeAmountFlow) { token, fee -> doubleArrayOf(token, fee) }
    }

    override suspend fun getSpendableBalanceByTokenCode(
        assetID: String,
        tokenCode: String,
        address: String
    ): Flow<Double> {
        val walletEntity = walletDao.getWalletByAddress(address)[0]
        if (assetID.isEmpty()) {
            return spentCoinsDao.getSpentCoinsByAddressCodeFlow(address, tokenCode)
                .map { list -> walletEntity.balance - (list.map { it.amount }.sum()) }
        }
        val tokenAmount = walletEntity.hashWithAmount[assetID] ?: 0.0
        return spentCoinsDao.getSpentCoinsByAddressCodeFlow(address, tokenCode)
            .map { list -> tokenAmount - (list.map { it.amount }.sum()) }
    }


}
