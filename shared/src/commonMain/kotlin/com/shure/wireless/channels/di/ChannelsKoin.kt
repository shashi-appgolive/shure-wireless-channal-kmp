package com.shure.wireless.channels.di

import com.shure.wireless.channels.core.database.DatabaseFactory
import com.shure.wireless.channels.core.database.di.databaseModule
import com.shure.wireless.channels.core.common.di.commonModule
import com.shure.wireless.channels.core.common.coroutines.AppBackgroundCoroutineScope
import com.shure.wireless.channels.core.common.coroutines.AppCoroutineScope
import com.shure.wireless.channels.core.datastore.DataStoreConfig
import com.shure.wireless.channels.core.datastore.DataStoreFactory
import com.shure.wireless.channels.core.datastore.di.dataStoreModule
import com.shure.wireless.channels.core.network.NetworkModuleConfig
import com.shure.wireless.channels.core.network.di.networkModule
import com.shure.wireless.channels.devices.di.storedDevicesModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module

object ChannelsKoin {
    private var application: KoinApplication? = null
    private var networkInstalled = false
    private var persistenceInstalled = false

    fun start(
        networkConfig: NetworkModuleConfig? = null,
        persistenceConfig: ChannelsPersistenceConfig? = null,
    ): KoinApplication {
        require(networkConfig != null || persistenceConfig != null) {
            "At least one Channels Koin module configuration is required."
        }

        val requestedModules = mutableListOf<Module>()
        if (application == null) {
            requestedModules += commonModule
        }
        if (networkConfig != null && !networkInstalled) {
            requestedModules += networkModule(networkConfig)
        }
        if (persistenceConfig != null && !persistenceInstalled) {
            requestedModules += persistenceModules(persistenceConfig)
        }

        application?.let { runningApplication ->
            if (requestedModules.isNotEmpty()) {
                runningApplication.koin.loadModules(
                    modules = requestedModules,
                    allowOverride = false,
                    createEagerInstances = true,
                )
                markInstalled(networkConfig, persistenceConfig)
            }
            return runningApplication
        }

        return startKoin {
            modules(requestedModules)
            createEagerInstances()
        }.also { startedApplication ->
            application = startedApplication
            markInstalled(networkConfig, persistenceConfig)
        }
    }

    fun startPersistence(config: ChannelsPersistenceConfig): KoinApplication =
        start(persistenceConfig = config)

    fun stop() {
        if (application == null) return

        application?.koin?.let { koin ->
            runCatching { koin.get<AppCoroutineScope>().close() }
            runCatching { koin.get<AppBackgroundCoroutineScope>().close() }
        }
        stopKoin()
        application = null
        networkInstalled = false
        persistenceInstalled = false
    }

    private fun persistenceModules(config: ChannelsPersistenceConfig): List<Module> =
        listOf(
            dataStoreModule(
                factory = config.dataStoreFactory,
                config = config.dataStoreConfig,
            ),
            databaseModule(config.databaseFactory),
            storedDevicesModule,
        )

    private fun markInstalled(
        networkConfig: NetworkModuleConfig?,
        persistenceConfig: ChannelsPersistenceConfig?,
    ) {
        if (networkConfig != null) networkInstalled = true
        if (persistenceConfig != null) persistenceInstalled = true
    }
}

data class ChannelsPersistenceConfig(
    val dataStoreFactory: DataStoreFactory,
    val databaseFactory: DatabaseFactory,
    val dataStoreConfig: DataStoreConfig = DataStoreConfig(),
)
