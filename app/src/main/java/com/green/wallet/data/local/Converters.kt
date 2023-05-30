package com.green.wallet.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


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


}
