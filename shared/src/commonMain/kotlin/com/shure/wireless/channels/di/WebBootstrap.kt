package com.shure.wireless.channels.di

import com.shure.wireless.channels.core.common.di.commonModule
import com.shure.wireless.channels.devices.di.webStoredDevicesModule
import org.koin.core.context.startKoin

/**
 * Starts Koin for the web (js/wasmJs) app with an in-memory device store.
 *
 * The web target has no worker-backed SQLiteDriver set up yet (that needs a
 * real Web Worker script plus a bundled sqlite-wasm binary), so it can't go
 * through [ChannelsKoin.startPersistence] the way Android/iOS do. This keeps
 * the app functional in the browser; devices just don't survive a reload.
 * Swap this for [ChannelsKoin.startPersistence] once a real web SQLiteDriver
 * is wired up.
 */
fun initializeWebInMemoryPersistence() {
    startKoin {
        modules(commonModule, webStoredDevicesModule)
    }
}
