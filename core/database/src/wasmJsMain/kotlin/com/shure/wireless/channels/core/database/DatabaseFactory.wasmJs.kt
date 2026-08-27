package com.shure.wireless.channels.core.database

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.SQLiteDriver

actual class DatabaseFactory(
    @PublishedApi internal val driver: SQLiteDriver,
) {
    actual inline fun <reified T : RoomDatabase> create(name: String): RoomDatabase.Builder<T> =
        Room.databaseBuilder<T>(name).setDriver(driver)
}

fun createDatabaseFactory(driver: SQLiteDriver): DatabaseFactory = DatabaseFactory(driver)
