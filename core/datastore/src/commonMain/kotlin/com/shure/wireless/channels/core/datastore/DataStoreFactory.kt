package com.shure.wireless.channels.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

expect class DataStoreFactory {
    fun create(config: DataStoreConfig): DataStore<Preferences>
}

fun DataStoreFactory.createPreferencesDataStore(
    config: DataStoreConfig = DataStoreConfig(),
): PreferencesDataStore = PreferencesDataStore(create(config))
