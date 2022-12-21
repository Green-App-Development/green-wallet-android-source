package com.green.wallet.data.network

import com.green.wallet.data.network.dto.coins.CoinRecordResponse
import com.green.wallet.data.network.dto.spendbundle.SpenBunde
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Created by bekjan on 06.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */


interface BlockChainService {


	@POST("create_new_wallet")
	suspend fun addNewToken(@Body body: HashMap<String, Any>): Response<BaseResponse>

	@POST("get_wallets")
	suspend fun getWallets(@Body body: HashMap<String, Any>): Response<JsonObject>

	@POST("delete_all_keys")
	suspend fun delete_all_keys(): Response<BaseResponse>

	@POST("get_coin_records_by_puzzle_hash")
	suspend fun queryBalanceWithSorting(@Body body: HashMap<String, Any>): Response<CoinRecordResponse>

	@POST("get_coin_records_by_puzzle_hash")
	suspend fun queryBalance(@Body body: HashMap<String, Any>): Response<JsonObject>


	@POST("get_coin_records_by_puzzle_hashes")
	suspend fun queryBalanceByPuzzleHashes(@Body body: HashMap<String, Any>): Response<CoinRecordResponse>

	@POST("push_tx")
	suspend fun pushTransaction(@Body body: SpenBunde): Response<BaseResponse>

	@POST("get_coin_records_by_names")
	suspend fun getCoinRecordsByNames(@Body body: HashMap<String, Any>): Response<JsonObject>

	@POST("get_coin_record_by_name")
	suspend fun getCoinRecordByName(@Body body: HashMap<String, Any>): Response<JsonObject>

	@POST("get_coin_records_by_parent_ids")
	suspend fun getCoinRecordsByParentIds(@Body body: HashMap<String, Any>): Response<CoinRecordResponse>

}
