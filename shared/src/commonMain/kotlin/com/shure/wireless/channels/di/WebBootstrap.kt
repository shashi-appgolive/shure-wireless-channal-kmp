package com.shure.wireless.channels.di

/**
 * Starts Koin for the web (js/wasmJs) app with an in-memory device store.
 *
 * The web target has no worker-backed SQLiteDriver set up yet (that needs a
 * real Web Worker script plus a bundled sqlite-wasm binary), so it can't go
 * through Room-backed persistence the way Android/iOS do. This keeps
 * the app functional in the browser; devices just don't survive a reload.
 * Swap this for [ChannelsKoin.startWithPersistence] once a real web SQLiteDriver
 * is wired up.
 */
fun initializeWebInMemoryPersistence() {
    ChannelsKoin.startWithInMemoryPersistence()
}
