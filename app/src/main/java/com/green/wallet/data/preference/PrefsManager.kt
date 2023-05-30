package com.green.wallet.data.preference

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.green.wallet.domain.interact.PrefsInteract
import com.green.wallet.presentation.di.application.AppScope
import com.green.wallet.presentation.tools.VLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject


@AppScope
class PrefsManager @Inject constructor(private val context: Context) : PrefsInteract {

	private val Context.dataStore by preferencesDataStore("app_preferences")

	private val preferences = context.dataStore.data
		.catch { exception ->
			if (exception is IOException) {
				VLog.d("Error reading preferences: ${exception.message}")
				emit(emptyPreferences())
			} else {
				throw exception
			}
		}


	override suspend fun saveSettingString(key: Preferences.Key<String>, value: String) {
		context.dataStore.edit { mutablePreferences -> mutablePreferences[key] = value }
	}

	override suspend fun saveSettingBoolean(key: Preferences.Key<Boolean>, value: Boolean) {
		context.dataStore.edit { mutablePreferences -> mutablePreferences[key] = value }
	}

	override suspend fun saveSettingInt(key: Preferences.Key<Int>, value: Int) {
		context.dataStore.edit { mutablePreferences ->
			mutablePreferences[key] = value
		}
	}

	override suspend fun saveSettingLong(key: Preferences.Key<Long>, value: Long) {
		context.dataStore.edit { mutablePreferences ->
			mutablePreferences[key] = value
		}
	}

	override suspend fun getSettingString(key: Preferences.Key<String>, default: String) =
		preferences.first()[key] ?: default

	override suspend fun getSettingBoolean(key: Preferences.Key<Boolean>, default: Boolean) =
		preferences.first()[key] ?: default

	override suspend fun getSettingInt(key: Preferences.Key<Int>, default: Int) =
		preferences.first()[key] ?: default

	override suspend fun getSettingLong(key: Preferences.Key<Long>, default: Long) =
		preferences.first()[key] ?: default

	override suspend fun getSettingBooleanFlow(
		key: Preferences.Key<Boolean>,
		default: Boolean
	): Flow<Boolean> {
		return preferences.map { it[key] ?: false }
	}

	override suspend fun getDoubleFlow(
		key: Preferences.Key<Double>,
		default: Double
	): Flow<Double> {
		return preferences.map { it[key] ?: 0.0 }
	}

	override suspend fun saveBalanceDoubleFlow(key: Preferences.Key<Double>, value: Double) {
		context.dataStore.edit { mutablePreferences ->
			mutablePreferences[key] = value
		}
	}

	override suspend fun saveCoursePriceDouble(key: Preferences.Key<Double>, value: Double) {
		context.dataStore.edit { mutablePreferences ->
			mutablePreferences[key] = value
		}
	}

	override suspend fun getCoursePriceDouble(
		key: Preferences.Key<Double>,
		default: Double
	): Double {
		return preferences.first()[key] ?: default
	}

	override suspend fun increaseHomeAddedCounter() {
		var home_add_counter = getHomeAddedCounter()
		context.dataStore.edit { mutablePreferences ->
			mutablePreferences[HOME_ADDED_COUNTER] = ++home_add_counter
		}
	}

	override suspend fun decreaseHomeAddedCounter() {
		var home_add_counter = getHomeAddedCounter()
		if (home_add_counter > 0) {
			context.dataStore.edit { mutablePreferences ->
				mutablePreferences[HOME_ADDED_COUNTER] = --home_add_counter
			}
		}
	}

	override suspend fun getHomeAddedCounter() = preferences.first()[HOME_ADDED_COUNTER] ?: 0

	override suspend fun saveObjectString(key: Preferences.Key<String>, jsonStrObject: String) {
		context.dataStore.edit { mutablePreferences ->
			mutablePreferences[key] = jsonStrObject
		}
	}

	override suspend fun getObjectString(key: Preferences.Key<String>) =
		preferences.first()[key] ?: ""

	override suspend fun clearingDataStore() {
		context.dataStore.edit {
			it.clear()
		}
	}


	companion object {
		val NIGHT_MODE_ON = booleanPreferencesKey("is_night_mode_on")
		val PASSCODE = stringPreferencesKey("passcode")
		val PUSH_NOTIF_IS_ON = booleanPreferencesKey("push_notif_is_on")
		val BALANCE_IS_HIDDEN = booleanPreferencesKey("balance_is_hidden")
		val USER_UNBOARDED = booleanPreferencesKey("user_unboarded")
		val PREV_MODE_CHANGED = booleanPreferencesKey("prev_mode_changed")
		val LAST_VISITED = longPreferencesKey("last_visited_long")
		val VERSION_REQUEST = stringPreferencesKey("version_request")
		val CUR_LANGUAGE_CODE = stringPreferencesKey("cur_language_code")
		val LANGUAGE_RESOURCE = stringPreferencesKey("language_resource")
		val HOME_ADDED_COUNTER = intPreferencesKey("home_added_counter")
		val ALL_NETWORK_ITEMS_LIST = stringPreferencesKey("all_network_items_list")
		val LANG_ITEMS_LIST = stringPreferencesKey("lang_items_list")
		val APP_INSTALL_TIME = longPreferencesKey("app_install_time")
		val NOTIFICATION_ID = intPreferencesKey("notification_id")
		val TIME_DIFFERENCE = longPreferencesKey("time_difference")
		val VERIFIED_DID_LIST = stringPreferencesKey("verified_did_list")
		val USER_GUID = stringPreferencesKey("user_guid")
	}

}
