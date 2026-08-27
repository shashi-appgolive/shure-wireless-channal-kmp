package com.shure.wireless.channels.core.database.di

import com.shure.wireless.channels.core.database.ChannelsDatabase
import com.shure.wireless.channels.core.database.DatabaseFactory
import com.shure.wireless.channels.core.database.buildDatabase
import com.shure.wireless.channels.core.database.dao.StoredDeviceDao
import org.koin.core.module.Module
import org.koin.dsl.module

fun databaseModule(factory: DatabaseFactory): Module = module {
    single<DatabaseFactory> { factory }
    single<ChannelsDatabase>(createdAtStart = true) {
        buildDatabase(
            get<DatabaseFactory>().create<ChannelsDatabase>(ChannelsDatabase.DATABASE_NAME),
        )
    }
    single<StoredDeviceDao> { get<ChannelsDatabase>().storedDeviceDao() }
}
