package com.shure.wireless.channels.di

import android.content.Context
import com.shure.wireless.channels.core.database.createDatabaseFactory
import com.shure.wireless.channels.core.datastore.DataStoreConfig
import com.shure.wireless.channels.core.datastore.createDataStoreFactory
import org.koin.core.KoinApplication

fun initializeAndroidPersistence(context: Context): KoinApplication =
    initializeAndroidAppGraph(context)

fun initializeAndroidAppGraph(context: Context): KoinApplication =
    ChannelsKoin.startWithPersistence(
        ChannelsPersistenceConfig(
            dataStoreFactory = context.createDataStoreFactory(),
            databaseFactory = context.createDatabaseFactory(),
            dataStoreConfig = DataStoreConfig(),
        ),
    )
