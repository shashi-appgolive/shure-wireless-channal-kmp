package com.shure.wireless.channels.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer
import okio.FileSystem
import okio.Path.Companion.toPath

actual class DataStoreFactory(
    private val context: Context,
) {
    actual fun create(config: DataStoreConfig): DataStore<Preferences> {
        val directory = config.customPath ?: context.applicationContext.filesDir.path
        val path = "$directory/${config.fileName}".toPath()
        return stores.getOrPut(path.toString()) {
            androidx.datastore.core.DataStoreFactory.create(
                storage = OkioStorage(
                    fileSystem = FileSystem.SYSTEM,
                    serializer = PreferencesSerializer,
                    producePath = { path },
                ),
            )
        }
    }

    private companion object {
        val stores = mutableMapOf<String, DataStore<Preferences>>()
    }
}

fun Context.createDataStoreFactory(): DataStoreFactory = DataStoreFactory(applicationContext)
