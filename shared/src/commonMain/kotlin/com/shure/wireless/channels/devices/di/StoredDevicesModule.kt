package com.shure.wireless.channels.devices.di

import com.shure.wireless.channels.devices.data.GraphQlDeviceOperationsRepository
import com.shure.wireless.channels.devices.data.InMemoryStoredDeviceRepository
import com.shure.wireless.channels.devices.data.RoomStoredDeviceRepository
import com.shure.wireless.channels.devices.data.graphql.DeviceDiscoveryGraphQlApi
import com.shure.wireless.channels.devices.domain.repository.DeviceOperationsRepository
import com.shure.wireless.channels.devices.domain.repository.StoredDeviceRepository
import com.shure.wireless.channels.devices.domain.usecase.ConnectDeviceUseCase
import com.shure.wireless.channels.devices.domain.usecase.DiscoverDevicesUseCase
import com.shure.wireless.channels.devices.domain.usecase.GetStoredDevicesUseCase
import com.shure.wireless.channels.devices.domain.usecase.ObserveStoredDevicesUseCase
import com.shure.wireless.channels.devices.domain.usecase.ListenDeviceEventsUseCase
import com.shure.wireless.channels.devices.domain.usecase.SaveStoredDeviceUseCase
import com.shure.wireless.channels.devices.domain.usecase.SaveStoredDevicesUseCase
import com.shure.wireless.channels.devices.presentation.DeviceViewModel
import com.shure.wireless.channels.core.network.di.GraphQlApiClientQualifier
import com.shure.wireless.channels.core.network.graphql.GraphQlClient
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val storedDevicesModule: Module = module {
    single<StoredDeviceRepository> { RoomStoredDeviceRepository(get()) }
    single {
        DeviceDiscoveryGraphQlApi(
            graphQlClient = get<GraphQlClient>(GraphQlApiClientQualifier),
        )
    }
    single<DeviceOperationsRepository> {
        GraphQlDeviceOperationsRepository(
            discoveryApi = get(),
        )
    }
    factory { ConnectDeviceUseCase(get()) }
    factory { DiscoverDevicesUseCase(get()) }
    factory { GetStoredDevicesUseCase(get()) }
    factory { ListenDeviceEventsUseCase(get()) }
    factory { ObserveStoredDevicesUseCase(get()) }
    factory { SaveStoredDeviceUseCase(get()) }
    factory { SaveStoredDevicesUseCase(get()) }
    viewModel {
        DeviceViewModel(
            getStoredDevicesUseCase = get(),
            observeStoredDevicesUseCase = get(),
            saveStoredDeviceUseCase = get(),
            saveStoredDevicesUseCase = get(),
            connectDeviceUseCase = get(),
            discoverDevicesUseCase = get(),
            listenDeviceEventsUseCase = get(),
        )
    }
}

/**
 * Same bindings as [storedDevicesModule], but backed by an in-memory device
 * store instead of Room. Used on the web target, which has no worker-backed
 * SQLiteDriver wired up yet.
 */
val webStoredDevicesModule: Module = module {
    single<StoredDeviceRepository> { InMemoryStoredDeviceRepository() }
    single {
        DeviceDiscoveryGraphQlApi(
            graphQlClient = get<GraphQlClient>(GraphQlApiClientQualifier),
        )
    }
    single<DeviceOperationsRepository> {
        GraphQlDeviceOperationsRepository(
            discoveryApi = get(),
        )
    }
    factory { ConnectDeviceUseCase(get()) }
    factory { DiscoverDevicesUseCase(get()) }
    factory { GetStoredDevicesUseCase(get()) }
    factory { ListenDeviceEventsUseCase(get()) }
    factory { ObserveStoredDevicesUseCase(get()) }
    factory { SaveStoredDeviceUseCase(get()) }
    factory { SaveStoredDevicesUseCase(get()) }
    viewModel {
        DeviceViewModel(
            getStoredDevicesUseCase = get(),
            observeStoredDevicesUseCase = get(),
            saveStoredDeviceUseCase = get(),
            saveStoredDevicesUseCase = get(),
            connectDeviceUseCase = get(),
            discoverDevicesUseCase = get(),
            listenDeviceEventsUseCase = get(),
        )
    }
}
