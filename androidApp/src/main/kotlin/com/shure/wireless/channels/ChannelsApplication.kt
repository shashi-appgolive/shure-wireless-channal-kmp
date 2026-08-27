package com.shure.wireless.channels

import android.app.Application
import com.shure.wireless.channels.di.initializeAndroidPersistence

class ChannelsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeAndroidPersistence(this)
    }
}
