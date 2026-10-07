package com.shure.wireless.channels

import android.app.Application
import com.shure.wireless.channels.di.initializeAndroidAppGraph

class ChannelsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeAndroidAppGraph(this)
    }
}
