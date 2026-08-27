package com.shure.wireless.channels.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.okio.WebLocalStorage
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer

actual class DataStoreFactory {
    actual fun create(config: DataStoreConfig): DataStore<Preferences> =
        stores.getOrPut(config.fileName) {
            androidx.datastore.core.DataStoreFactory.create(
                storage = WebLocalStorage(
                    serializer = PreferencesSerializer,
                    name = config.fileName,
                ),
            )
        }

    private companion object {
        val stores = mutableMapOf<String, DataStore<Preferences>>()
    }
}

fun createDataStoreFactory(): DataStoreFactory = DataStoreFactory()
