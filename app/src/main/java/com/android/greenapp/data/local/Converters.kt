package com.android.greenapp.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


/**
 * Created by bekjan on 09.06.2022.
 * email: bekjan.omirzak98@gmail.com
 */
object Converters {

    fun listToJson(value: List<String>) = Gson().toJson(value)


    fun jsonToList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun convertStringToMutableList(jsonList: String): MutableList<String> {
        val type = object : TypeToken<MutableList<String>>() {}.type
        return Gson().fromJson(jsonList, type)
    }

    @TypeConverter
    fun convertMutableListToJsonString(list: MutableList<String>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun convertMutableSetToJsonString(list: MutableSet<String>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun convertJsonStringToSetOfHash(jsonSet: String): MutableSet<String> {
        val type = object : TypeToken<MutableSet<String>>() {}.type
        return Gson().fromJson(jsonSet, type)
    }

    @TypeConverter
    fun convertHashWithAmountToJson(hashWithAmount: HashMap<String, Double>): String {
        return Gson().toJson(hashWithAmount)
    }

    @TypeConverter
    fun convertJsonToHashWithAmount(hashWithAmount: String): HashMap<String, Double> {
        val type = object : TypeToken<HashMap<String, Double>>() {}.type
        return Gson().fromJson(hashWithAmount, type)
    }


    @TypeConverter
    fun convertJsonToHashWithID(hashWithAmount: String): HashMap<String, Int> {
        val type = object : TypeToken<HashMap<String, Int>>() {}.type
        return Gson().fromJson(hashWithAmount, type)
    }

    @TypeConverter
    fun convertHashWithIDToString(hashWithId: HashMap<String, Int>): String {
        return Gson().toJson(hashWithId)
    }


    fun stringToHashMap(str: String): HashMap<String, String> {
        val type = object : TypeToken<HashMap<String, String>>() {}.type
        return Gson().fromJson(str, type)
    }

    fun hashMapToString(hashMap: HashMap<String, String>): String {
        return Gson().toJson(hashMap)
    }


}