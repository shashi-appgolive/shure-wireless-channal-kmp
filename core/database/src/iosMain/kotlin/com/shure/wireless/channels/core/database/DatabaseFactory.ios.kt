package com.shure.wireless.channels.core.database

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual class DatabaseFactory {
    actual inline fun <reified T : RoomDatabase> create(name: String): RoomDatabase.Builder<T> =
        Room.databaseBuilder<T>(
            name = "${applicationSupportDirectory()}/$name",
        ).setDriver(BundledSQLiteDriver())

    @PublishedApi
    @OptIn(ExperimentalForeignApi::class)
    internal fun applicationSupportDirectory(): String {
        val url = NSFileManager.defaultManager.URLForDirectory(
            directory = NSApplicationSupportDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null,
        )
        return requireNotNull(url?.path)
    }
}

fun createDatabaseFactory(): DatabaseFactory = DatabaseFactory()
