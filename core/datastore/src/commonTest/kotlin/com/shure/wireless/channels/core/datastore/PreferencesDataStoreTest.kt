package com.shure.wireless.channels.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest

class PreferencesDataStoreTest {
    @Test
    fun writesObservesAndRemovesTypedValues() = runTest {
        val preferences = PreferencesDataStore(InMemoryPreferencesDataStore())

        preferences.putString("device_name", "ULXD4")
        preferences.putBoolean("auto_scan", true)
        preferences.putInt("scan_interval", 15)

        assertEquals("ULXD4", preferences.getStringValue("device_name"))
        assertTrue(preferences.getBooleanValue("auto_scan"))
        assertEquals(15, preferences.getIntValue("scan_interval"))
        assertTrue(preferences.contains("auto_scan"))

        preferences.remove("auto_scan")

        assertFalse(preferences.contains("auto_scan"))
        assertFalse(preferences.getBooleanValue("auto_scan"))
    }
}

private class InMemoryPreferencesDataStore : DataStore<Preferences> {
    private val state = MutableStateFlow(emptyPreferences())

    override val data: Flow<Preferences> = state

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        val updated = transform(state.value)
        state.value = updated
        return updated
    }
}
