package com.android.greenapp.data.network

import com.android.greenapp.data.network.dto.blockchain.MnemonicDto
import com.android.greenapp.data.network.dto.blockchain.PublicKeyDto
import com.android.greenapp.data.network.dto.transaction.TransactionDto
import com.android.greenapp.presentation.main.send.spend.SpenBunde
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Created by bekjan on 06.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */


interface BlockChainService {


    @POST("generate_mnemonic")
    suspend fun getGeneratedMnemonics(@Body body: Any = Object()): Response<MnemonicDto>

    @POST("add_key")
    suspend fun getPublicKeyFingerPrints(
        @Body body: HashMap<String, Any>
    ): Response<PublicKeyDto>

    @POST("get_private_key")
    suspend fun getPrivateKey(@Body body: HashMap<String, Any>): Response<BaseResponse>

    @POST("get_next_address")
    suspend fun generateNewAddress(@Body body: HashMap<String, Any>): Response<JsonObject>

    @POST("log_in")
    suspend fun loginWithFingerPrint(@Body body: HashMap<String, Any>): Response<BaseResponse>

    @POST("get_public_keys")
    suspend fun getAllPublicKeys(@Body body: Any = Object()): Response<BaseResponse>

    @POST("get_wallet_balance")
    suspend fun getWalletBalance(@Body body: HashMap<String, Any>): Response<BaseResponse>

    @POST("get_transactions")
    suspend fun getTransactions(@Body body: HashMap<String, Any>): Response<BaseResponse>

    @POST("get_transaction")
    suspend fun getTransaction(@Body body: HashMap<String, Any>): Response<TransactionDto>

    @POST("send_transaction")
    suspend fun sendTransaction(@Body body: HashMap<String, Any>): Response<BaseResponse>

    @POST("create_new_wallet")
    suspend fun addNewToken(@Body body: HashMap<String, Any>): Response<BaseResponse>

    @POST("get_wallets")
    suspend fun getWallets(@Body body: HashMap<String, Any>): Response<JsonObject>

    @POST("delete_all_keys")
    suspend fun delete_all_keys(): Response<BaseResponse>

    @POST("get_coin_records_by_puzzle_hash")
    suspend fun queryBalance(@Body body: HashMap<String, Any>): Response<JsonObject>


    @POST("push_tx")
    suspend fun pushTransaction(@Body body: SpenBunde): Response<BaseResponse>

    @POST("get_coin_records_by_names")
    suspend fun getCoinRecordsByNames(@Body body: HashMap<String, Any>): Response<JsonObject>

    @POST("get_coin_record_by_name")
    suspend fun getCoinRecordByName(@Body body: HashMap<String, Any>): Response<JsonObject>

}