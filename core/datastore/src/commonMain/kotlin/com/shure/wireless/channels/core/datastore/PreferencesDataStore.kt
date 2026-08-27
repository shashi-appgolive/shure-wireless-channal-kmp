package com.shure.wireless.channels.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PreferencesDataStore(
    private val dataStore: DataStore<Preferences>,
) {
    private val safeData: Flow<Preferences>
        get() = dataStore.data.catch { exception ->
            if (exception is CancellationException) throw exception
            emit(emptyPreferences())
        }

    suspend fun putString(key: String, value: String) = update(stringPreferencesKey(key), value)
    fun getString(key: String, defaultValue: String = ""): Flow<String> =
        observe(stringPreferencesKey(key), defaultValue)
    suspend fun getStringValue(key: String, defaultValue: String = ""): String =
        getString(key, defaultValue).first()

    suspend fun putInt(key: String, value: Int) = update(intPreferencesKey(key), value)
    fun getInt(key: String, defaultValue: Int = 0): Flow<Int> =
        observe(intPreferencesKey(key), defaultValue)
    suspend fun getIntValue(key: String, defaultValue: Int = 0): Int =
        getInt(key, defaultValue).first()

    suspend fun putBoolean(key: String, value: Boolean) = update(booleanPreferencesKey(key), value)
    fun getBoolean(key: String, defaultValue: Boolean = false): Flow<Boolean> =
        observe(booleanPreferencesKey(key), defaultValue)
    suspend fun getBooleanValue(key: String, defaultValue: Boolean = false): Boolean =
        getBoolean(key, defaultValue).first()

    suspend fun putLong(key: String, value: Long) = update(longPreferencesKey(key), value)
    fun getLong(key: String, defaultValue: Long = 0L): Flow<Long> =
        observe(longPreferencesKey(key), defaultValue)
    suspend fun getLongValue(key: String, defaultValue: Long = 0L): Long =
        getLong(key, defaultValue).first()

    suspend fun putFloat(key: String, value: Float) = update(floatPreferencesKey(key), value)
    fun getFloat(key: String, defaultValue: Float = 0f): Flow<Float> =
        observe(floatPreferencesKey(key), defaultValue)
    suspend fun getFloatValue(key: String, defaultValue: Float = 0f): Float =
        getFloat(key, defaultValue).first()

    suspend fun putStringSet(key: String, value: Set<String>) =
        update(stringSetPreferencesKey(key), value)
    fun getStringSet(key: String, defaultValue: Set<String> = emptySet()): Flow<Set<String>> =
        observe(stringSetPreferencesKey(key), defaultValue)
    suspend fun getStringSetValue(
        key: String,
        defaultValue: Set<String> = emptySet(),
    ): Set<String> = getStringSet(key, defaultValue).first()

    suspend fun remove(key: String) {
        dataStore.edit { preferences ->
            preferences.asMap().keys
                .firstOrNull { preferenceKey -> preferenceKey.name == key }
                ?.let(preferences::remove)
        }
    }

    suspend fun clear() {
        dataStore.edit { preferences -> preferences.clear() }
    }

    suspend fun contains(key: String): Boolean =
        safeData.first().asMap().keys.any { preferenceKey -> preferenceKey.name == key }

    fun getAll(): Flow<Preferences> = safeData

    private suspend fun <T> update(key: Preferences.Key<T>, value: T) {
        dataStore.edit { preferences -> preferences[key] = value }
    }

    private fun <T> observe(key: Preferences.Key<T>, defaultValue: T): Flow<T> =
        safeData.map { preferences -> preferences[key] ?: defaultValue }
}
