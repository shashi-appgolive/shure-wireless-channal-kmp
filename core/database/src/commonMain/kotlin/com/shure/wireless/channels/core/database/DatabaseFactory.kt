package com.shure.wireless.channels.core.database

import androidx.room3.RoomDatabase
import kotlinx.coroutines.Dispatchers

expect class DatabaseFactory {
    inline fun <reified T : RoomDatabase> create(name: String): RoomDatabase.Builder<T>
}

fun <T : RoomDatabase> buildDatabase(builder: RoomDatabase.Builder<T>): T =
    builder
        .setQueryCoroutineContext(Dispatchers.Default)
        .build()
