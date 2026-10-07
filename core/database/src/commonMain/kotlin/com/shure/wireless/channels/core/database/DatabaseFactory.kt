package com.shure.wireless.channels.core.database

import androidx.room3.RoomDatabase
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import kotlinx.coroutines.Dispatchers

expect class DatabaseFactory {
    inline fun <reified T : RoomDatabase> create(name: String): RoomDatabase.Builder<T>
}

fun <T : RoomDatabase> buildDatabase(builder: RoomDatabase.Builder<T>): T =
    builder
        .addMigrations(StoredDevicesMigration1To2)
        .setQueryCoroutineContext(Dispatchers.Default)
        .build()

private object StoredDevicesMigration1To2 : Migration(1, 2) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE stored_devices ADD COLUMN hardwareId TEXT")
        connection.execSQL("ALTER TABLE stored_devices ADD COLUMN status TEXT")
        connection.execSQL("ALTER TABLE stored_devices ADD COLUMN category TEXT")
    }
}
