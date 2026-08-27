package com.shure.wireless.channels.core.datastore.di

import com.shure.wireless.channels.core.datastore.DataStoreConfig
import com.shure.wireless.channels.core.datastore.DataStoreFactory
import com.shure.wireless.channels.core.datastore.PreferencesDataStore
import com.shure.wireless.channels.core.datastore.createPreferencesDataStore
import org.koin.core.module.Module
import org.koin.dsl.module

fun dataStoreModule(
    factory: DataStoreFactory,
    config: DataStoreConfig = DataStoreConfig(),
): Module = module {
    single<DataStoreFactory> { factory }
    single<DataStoreConfig> { config }
    single<PreferencesDataStore> {
        get<DataStoreFactory>().createPreferencesDataStore(get())
    }
}
