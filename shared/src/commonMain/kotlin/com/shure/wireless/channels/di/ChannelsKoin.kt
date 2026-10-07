package com.shure.wireless.channels.di

import com.shure.wireless.channels.core.database.DatabaseFactory
import com.shure.wireless.channels.core.database.di.databaseModule
import com.shure.wireless.channels.core.common.di.commonModule
import com.shure.wireless.channels.core.common.coroutines.AppBackgroundCoroutineScope
import com.shure.wireless.channels.core.common.coroutines.AppCoroutineScope
import com.shure.wireless.channels.core.datastore.DataStoreConfig
import com.shure.wireless.channels.core.datastore.DataStoreFactory
import com.shure.wireless.channels.core.datastore.di.dataStoreModule
import com.shure.wireless.channels.core.network.di.networkModule
import com.shure.wireless.channels.devices.di.storedDevicesModule
import com.shure.wireless.channels.devices.di.webStoredDevicesModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module

object ChannelsKoin {
    private var application: KoinApplication? = null
    private var started = false

    fun start(config: ChannelsAppConfig): KoinApplication {
        if (started) return requireNotNull(application)

        return startKoin {
            modules(appModules(config))
            createEagerInstances()
        }.also { startedApplication ->
            application = startedApplication
            started = true
        }
    }

    fun startWithPersistence(config: ChannelsPersistenceConfig): KoinApplication =
        start(ChannelsAppConfig.Persistence(config))

    fun startWithInMemoryPersistence(): KoinApplication =
        start(ChannelsAppConfig.InMemoryPersistence)

    fun stop() {
        if (application == null) return

        application?.koin?.let { koin ->
            runCatching { koin.get<AppCoroutineScope>().close() }
            runCatching { koin.get<AppBackgroundCoroutineScope>().close() }
        }
        stopKoin()
        application = null
        started = false
    }

    private fun appModules(config: ChannelsAppConfig): List<Module> =
        when (config) {
            is ChannelsAppConfig.Persistence ->
                listOf(
                    commonModule,
                    networkModule(defaultAppNetworkConfig()),
                    dataStoreModule(
                        factory = config.persistence.dataStoreFactory,
                        config = config.persistence.dataStoreConfig,
                    ),
                    databaseModule(config.persistence.databaseFactory),
                    storedDevicesModule,
                )

            ChannelsAppConfig.InMemoryPersistence ->
                listOf(
                    commonModule,
                    networkModule(defaultAppNetworkConfig()),
                    webStoredDevicesModule,
                )
        }
}

sealed interface ChannelsAppConfig {
    data class Persistence(val persistence: ChannelsPersistenceConfig) : ChannelsAppConfig
    data object InMemoryPersistence : ChannelsAppConfig
}

data class ChannelsPersistenceConfig(
    val dataStoreFactory: DataStoreFactory,
    val databaseFactory: DatabaseFactory,
    val dataStoreConfig: DataStoreConfig = DataStoreConfig(),
)
