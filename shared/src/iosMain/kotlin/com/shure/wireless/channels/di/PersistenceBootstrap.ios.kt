package com.shure.wireless.channels.di

import com.shure.wireless.channels.core.database.createDatabaseFactory
import com.shure.wireless.channels.core.datastore.DataStoreConfig
import com.shure.wireless.channels.core.datastore.createDataStoreFactory

/** Called by the Swift application before the first Compose screen is created. */
fun initializeIosPersistence() {
    ChannelsKoin.startPersistence(
        ChannelsPersistenceConfig(
            dataStoreFactory = createDataStoreFactory(),
            databaseFactory = createDatabaseFactory(),
            dataStoreConfig = DataStoreConfig(),
        ),
    )
}
