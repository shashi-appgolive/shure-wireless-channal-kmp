package com.shure.wireless.channels.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer
import kotlinx.cinterop.ExperimentalForeignApi
import okio.FileSystem
import okio.Path.Companion.toPath
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual class DataStoreFactory {
    actual fun create(config: DataStoreConfig): DataStore<Preferences> {
        val directory = config.customPath ?: applicationSupportDirectory()
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

    @OptIn(ExperimentalForeignApi::class)
    private fun applicationSupportDirectory(): String {
        val url = NSFileManager.defaultManager.URLForDirectory(
            directory = NSApplicationSupportDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null,
        )
        return requireNotNull(url?.path)
    }

    private companion object {
        val stores = mutableMapOf<String, DataStore<Preferences>>()
    }
}

fun createDataStoreFactory(): DataStoreFactory = DataStoreFactory()
