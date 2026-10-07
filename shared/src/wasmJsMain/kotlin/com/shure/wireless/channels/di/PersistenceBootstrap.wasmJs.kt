package com.shure.wireless.channels.di

import androidx.sqlite.SQLiteDriver
import com.shure.wireless.channels.core.database.createDatabaseFactory
import com.shure.wireless.channels.core.datastore.DataStoreConfig
import com.shure.wireless.channels.core.datastore.createDataStoreFactory
import org.koin.core.KoinApplication

/**
 * Starts browser persistence with the SQLite worker-backed driver owned by the web application.
 */
fun initializeWebPersistence(driver: SQLiteDriver): KoinApplication =
    ChannelsKoin.startWithPersistence(
        ChannelsPersistenceConfig(
            dataStoreFactory = createDataStoreFactory(),
            databaseFactory = createDatabaseFactory(driver),
            dataStoreConfig = DataStoreConfig(),
        ),
    )
