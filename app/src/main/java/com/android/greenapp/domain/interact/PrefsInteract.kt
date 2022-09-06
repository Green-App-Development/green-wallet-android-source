package com.android.greenapp.domain.interact

import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow

interface PrefsInteract {

    suspend fun saveSettingString(key: Preferences.Key<String>, value: String)
    suspend fun saveSettingBoolean(key: Preferences.Key<Boolean>, value: Boolean)
    suspend fun saveSettingInt(key: Preferences.Key<Int>, value: Int)
    suspend fun saveSettingLong(key: Preferences.Key<Long>, value: Long)

    suspend fun getSettingString(key: Preferences.Key<String>, default: String): String
    suspend fun getSettingBoolean(key: Preferences.Key<Boolean>, default: Boolean): Boolean
    suspend fun getSettingInt(key: Preferences.Key<Int>, default: Int): Int
    suspend fun getSettingLong(key: Preferences.Key<Long>, default: Long): Long
    suspend fun getSettingBooleanFlow(
        key: Preferences.Key<Boolean>,
        default: Boolean
    ): Flow<Boolean>


    suspend fun getDoubleFlow(key: Preferences.Key<Double>, default: Double): Flow<Double>
    suspend fun saveBalanceDoubleFlow(key: Preferences.Key<Double>, value: Double)

    suspend fun saveCoursePriceDouble(key: Preferences.Key<Double>, value: Double)
    suspend fun getCoursePriceDouble(key: Preferences.Key<Double>, default: Double): Double

    suspend fun increaseHomeAddedCounter()
    suspend fun decreaseHomeAddedCounter()
    suspend fun getHomeAddedCounter(): Int

    suspend fun saveObjectString(key: Preferences.Key<String>,jsonObj:String)
    suspend fun getObjectString(key: Preferences.Key<String>): String

    suspend fun clearingDataStore()

}