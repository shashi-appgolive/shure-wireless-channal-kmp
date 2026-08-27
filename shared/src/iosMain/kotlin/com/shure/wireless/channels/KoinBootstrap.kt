package com.shure.wireless.channels

import com.shure.wireless.channels.di.initializeIosPersistence

/** Stable Swift-facing entry point for initializing the shared persistence graph. */
fun startChannelsPersistence() {
    initializeIosPersistence()
}
