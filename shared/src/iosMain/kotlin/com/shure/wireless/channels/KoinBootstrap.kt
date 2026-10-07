package com.shure.wireless.channels

import com.shure.wireless.channels.di.initializeIosAppGraph

/** Stable Swift-facing entry point for initializing the shared app graph. */
fun startChannelsPersistence() {
    startChannelsAppGraph()
}

fun startChannelsAppGraph() {
    initializeIosAppGraph()
}
