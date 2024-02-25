package com.green.wallet.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.green.wallet.presentation.main.dapp.trade.models.CatToken
import com.green.wallet.presentation.main.dapp.trade.models.NftToken
import com.green.wallet.presentation.main.dapp.trade.models.Token
import org.json.JSONArray
import org.json.JSONObject


object Converters {


    @TypeConverter
    fun convertListToJsonString(value: List<String>) = Gson().toJson(value)

    @TypeConverter
    fun convertJsonToList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun convertHashWithAmountToJson(hashWithAmount: HashMap<String, Double>): String {
        return Gson().toJson(hashWithAmount)
    }

    @TypeConverter
    fun convertHashListImportedToJson(hashListImported: HashMap<String, List<String>>): String {
        return Gson().toJson(hashListImported)
    }

    @TypeConverter
    fun convertTokensStartHeightToJson(tokenStartHeight: HashMap<String, Long>): String {
        return Gson().toJson(tokenStartHeight)
    }

    @TypeConverter
    fun convertJsonToHashWithAmount(hashWithAmount: String): HashMap<String, Double> {
        val type = object : TypeToken<HashMap<String, Double>>() {}.type
        return Gson().fromJson(hashWithAmount, type)
    }

    @TypeConverter
    fun convertJsonToTokenStartHeight(tokenStartHeight: String): HashMap<String, Long> {
        val type = object : TypeToken<HashMap<String, Long>>() {}.type
        return Gson().fromJson(tokenStartHeight, type)
    }

    @TypeConverter
    fun convertJsonToHashListImported(hashListImported: String): HashMap<String, List<String>> {
        val type = object : TypeToken<HashMap<String, List<String>>>() {}.type
        return Gson().fromJson(hashListImported, type)
    }


    @TypeConverter
    fun stringToHashMap(str: String): HashMap<String, String> {
        val type = object : TypeToken<HashMap<String, String>>() {}.type
        return Gson().fromJson(str, type)
    }

    @TypeConverter
    fun hashMapToString(hashMap: HashMap<String, String>): String {
        return Gson().toJson(hashMap)
    }

    @TypeConverter
    fun listOfTokenToString(list: List<Token>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun stringToListOfToken(str: String): List<Token> {
        val list = mutableListOf<Token>()
        val array = JSONArray(str)
        for (i in 0 until array.length()) {
            val json = JSONObject(array[i].toString())
            if (json.has("code")) {
                val obj = Gson().fromJson(array[i].toString(), CatToken::class.java)
                list.add(obj)
            } else {
                val obj = Gson().fromJson(array[i].toString(), NftToken::class.java)
                list.add(obj)
            }
        }
        return list
    }
}
