package com.shure.wireless.channels.core.database

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import com.shure.wireless.channels.core.database.dao.StoredDeviceDao
import com.shure.wireless.channels.core.database.entity.StoredDeviceEntity

@Database(
    entities = [StoredDeviceEntity::class],
    version = 2,
    exportSchema = true,
)
@ConstructedBy(ChannelsDatabaseConstructor::class)
abstract class ChannelsDatabase : RoomDatabase() {
    abstract fun storedDeviceDao(): StoredDeviceDao

    companion object {
        const val DATABASE_NAME = "shure_channels.db"
    }
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object ChannelsDatabaseConstructor : RoomDatabaseConstructor<ChannelsDatabase> {
    override fun initialize(): ChannelsDatabase
}
