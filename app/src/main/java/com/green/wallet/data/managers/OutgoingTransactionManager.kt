package com.green.wallet.data.managers

import com.green.wallet.data.local.Converters
import com.green.wallet.data.local.TransactionDao
import com.green.wallet.data.local.entity.TransactionEntity
import com.green.wallet.data.local.entity.WalletEntity
import com.green.wallet.data.network.BlockChainService
import com.green.wallet.data.network.dto.coins.CoinRecord
import com.green.wallet.data.preference.PrefsManager
import com.green.wallet.domain.interact.GreenAppInteract
import com.green.wallet.domain.interact.PrefsInteract
import com.green.wallet.presentation.custom.NotificationHelper
import com.green.wallet.presentation.custom.formattedDoubleAmountWithPrecision
import com.green.wallet.presentation.tools.Status
import com.example.common.tools.VLog
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

/**
 * Created by bekjan on 14.12.2022.
 * email: bekjan.omirzak98@gmail.com
 */

class OutgoingTransactionManager @Inject constructor(
	private val transactionDao: TransactionDao,
	private val notificationHelper: NotificationHelper,
	private val prefsInteract: PrefsInteract,
	private val greenAppInteract: GreenAppInteract
) {


	suspend fun checkOutGoingTransactions(
		puzzle_hashes: List<String>,
		startHeight: Long,
		wallet: WalletEntity,
		blockChainService: BlockChainService,
		division: Double,
		tokenCode: String
	) {
		try {
			VLog.d("Checkout Outgoing trans from startHeight : $startHeight for wallet : $wallet with tokenCode : $tokenCode")
			val checkoutInProgress =
				transactionDao.checkInProgressTransactionsByAddress(wallet.address)
			if (checkoutInProgress.isPresent || startHeight == 0L) {
				VLog.d("Assuming this trans done by himself or no coins to spent")
				return
			}

			val resLanguageResource =
				prefsInteract.getSettingString(
					PrefsManager.LANGUAGE_RESOURCE,
					""
				)
			if (resLanguageResource.isEmpty()) return
			val resMap = Converters.stringToHashMap(resLanguageResource)
			val outgoing_transaction =
				resMap["push_notifications_outgoing"]
					?: "Outgoing transaction"

			val body = hashMapOf<String, Any>()
			body["puzzle_hashes"] =
				puzzle_hashes
			body["include_spent_coins"] = true
			body["start_height"] = startHeight
			val res = blockChainService.queryBalanceByPuzzleHashes(body)
			if (res.isSuccessful) {
				val filteredCoinRecords =
					res.body()!!.coin_records.filter { it.timestamp * 1000 >= wallet.savedTime }

				val confirmedHeightAndSum = getConfirmedBlockIndexWithSum(filteredCoinRecords)
				val spentAmountWithSum = getSpentBlockIndexWithSum(filteredCoinRecords)

				VLog.d("Coin Records spent coins true : $filteredCoinRecords")
				for (coin in filteredCoinRecords) {
					val coinAmount = coin.coin.amount
					val timeStamp = coin.timestamp
					val height = coin.confirmed_block_index
					val parent_coin_info = coin.coin.parent_coin_info
					//first case when coins back from sending
					val parentCoinIncoming =
						getParentCoinsSpentAmountAndHashValue(
							parent_coin_info,
							blockChainService
						)!!
					val parent_puzzle_hash = parentCoinIncoming["puzzle_hash"] as String
					VLog.d("Getting coins spent by me : $parentCoinIncoming")
					if (puzzle_hashes.contains(parent_puzzle_hash.substring(2))) {
						body["parent_ids"] =
							listOf(coin.coin.parent_coin_info)
						body["include_spent_coins"] = true
						val coinsByParentIdRes =
							blockChainService.getCoinRecordsByParentIds(body)
						VLog.d("Getting parent coins by parent id : $: $coinsByParentIdRes")
						if (coinsByParentIdRes.isSuccessful) {
							VLog.d("Got parent coins split by parent_id: ${coinsByParentIdRes.body()!!}")
							for (parent_coin in coinsByParentIdRes.body()!!.coin_records) {
								if (!puzzle_hashes.contains(
										parent_coin.coin.puzzle_hash.substring(
											2
										)
									)
								) {
									VLog.d("Found Sent Amount OutGoing Transaction  : $parent_coin")
									val sentAmount = parent_coin.coin.amount / division
									val outGoingConfirmedBlockIndex =
										parent_coin.confirmed_block_index
									val outGoingTrans =
										transactionDao.checkoutOutGoingTransactionsByAddressHeightAmountDestHash(
											wallet.address,
											sentAmount,
											outGoingConfirmedBlockIndex,
											parent_coin.coin.puzzle_hash.substring(2),
											tokenCode
										)
									if (!outGoingTrans.isPresent) {
										VLog.d("Checkout outgoing trans : $outGoingTrans after fining out sent transaction and $coin")
										val transEntity = TransactionEntity(
											UUID.randomUUID().toString(),
											sentAmount,
											greenAppInteract.getServerTime() * 1000L,
											outGoingConfirmedBlockIndex,
											Status.Outgoing,
											wallet.networkType,
											parent_coin.coin.puzzle_hash.substring(2),
											wallet.address,
											0.0,
											tokenCode
										)
										val formatted =
											formattedDoubleAmountWithPrecision(coinAmount / division)
										notificationHelper.callGreenAppNotificationMessages(
											"$outgoing_transaction : $formatted $tokenCode",
											System.currentTimeMillis()
										)
										VLog.d("Inserting New Outgoing Transaction  : $transEntity")
										transactionDao.insertTransaction(transEntity)
									}
								}
							}
						} else {
							VLog.d("Request is not success checking coins by parent_id : ${coinsByParentIdRes.body()}")
						}
					}
				}
				//second case when coin sent hasn't be split
				for ((block, sum) in spentAmountWithSum) {
					if (confirmedHeightAndSum.containsKey(block)) continue
					val prevTrans =
						transactionDao.checkoutOutgoingTransactionsByAddressHeightSecondCase(
							wallet.address,
							amount = sum / division,
							height = block
						)
					if (!prevTrans.isPresent) {
						val formatted =
							formattedDoubleAmountWithPrecision(sum / division)
						notificationHelper.callGreenAppNotificationMessages(
							"$outgoing_transaction : $formatted $tokenCode",
							System.currentTimeMillis()
						)
						val OutgoingTranEntity = TransactionEntity(
							UUID.randomUUID().toString(),
							sum / division,
							greenAppInteract.getServerTime() * 1000,
							block,
							Status.Outgoing,
							wallet.networkType,
							"",
							wallet.address,
							0.0,
							tokenCode
						)
						transactionDao.insertTransaction(OutgoingTranEntity)
					}
				}
			} else {
				VLog.d("Request is not successful in query balance for outgoing trans")
			}
		} catch (ex: Exception) {
			VLog.d("Exception in checking outgoing transactions : ${ex.message}")
		}
	}

	private fun getConfirmedBlockIndexWithSum(records: List<CoinRecord>): HashMap<Long, Long> {
		val confirmedBlockWithAmount = hashMapOf<Long, Long>()
		for (coin in records) {
			if (!coin.spent) {
				val confirmedHeight = coin.confirmed_block_index
				val amount = coin.coin.amount
				confirmedBlockWithAmount[confirmedHeight] =
					confirmedBlockWithAmount.getOrDefault(confirmedHeight, 0L) + amount
			}
		}
		return confirmedBlockWithAmount
	}

	private fun getSpentBlockIndexWithSum(records: List<CoinRecord>): HashMap<Long, Long> {
		val spentBlockWithAmount = hashMapOf<Long, Long>()
		for (coin in records) {
			if (coin.spent) {
				val spentHeight = coin.spent_block_index.toLong()
				val amount = coin.coin.amount
				spentBlockWithAmount[spentHeight] =
					spentBlockWithAmount.getOrDefault(spentHeight, 0L) + amount
			}
		}
		return spentBlockWithAmount
	}


	suspend fun getParentCoinsSpentAmountAndHashValue(
		parent_info: String,
		service: BlockChainService
	): HashMap<Any, Any>? {
		val res = hashMapOf<Any, Any>()
		val body = hashMapOf<String, Any>()
		body["name"] = parent_info
		body["include_spent_coins"] = true
		val request = service.getCoinRecordByName(body)
		if (request.isSuccessful) {
			val coinRecordJson = request.body()!!["coin_record"].asJsonObject
			val amount = coinRecordJson.get("coin").asJsonObject.get("amount").asLong
			val puzzle_hash = coinRecordJson.get("coin").asJsonObject.get("puzzle_hash").asString
			res["amount"] = amount
			res["puzzle_hash"] = puzzle_hash
			return res
		} else {
			VLog.d("Request in getting parent coin info is not success : ${request.body()}")
		}
		return null
	}


}
