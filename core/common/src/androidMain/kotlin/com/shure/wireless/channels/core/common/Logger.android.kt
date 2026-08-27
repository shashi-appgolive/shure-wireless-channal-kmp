package com.shure.wireless.channels.core.common

import android.util.Log

actual object Logger {
    actual var isDebugEnabled: Boolean = true

    actual fun d(tag: String, message: String) {
        if (isDebugEnabled) {
            AppLogStore.record(LogLevel.DEBUG, tag, message)
            Log.d(tag, message)
        }
    }

    actual fun i(tag: String, message: String) {
        AppLogStore.record(LogLevel.INFO, tag, message)
        Log.i(tag, message)
    }

    actual fun w(tag: String, message: String) {
        AppLogStore.record(LogLevel.WARN, tag, message)
        Log.w(tag, message)
    }

    actual fun e(tag: String, message: String, throwable: Throwable?) {
        AppLogStore.record(LogLevel.ERROR, tag, message, throwable)
        Log.e(tag, message, throwable)
    }
}
