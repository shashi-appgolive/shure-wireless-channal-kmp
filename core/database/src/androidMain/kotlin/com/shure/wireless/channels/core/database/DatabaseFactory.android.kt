package com.shure.wireless.channels.core.database

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

actual class DatabaseFactory(
    @PublishedApi internal val context: Context,
) {
    actual inline fun <reified T : RoomDatabase> create(name: String): RoomDatabase.Builder<T> =
        Room.databaseBuilder<T>(
            context = context.applicationContext,
            name = context.applicationContext.getDatabasePath(name).absolutePath,
        ).setDriver(BundledSQLiteDriver())
}

fun Context.createDatabaseFactory(): DatabaseFactory = DatabaseFactory(applicationContext)
