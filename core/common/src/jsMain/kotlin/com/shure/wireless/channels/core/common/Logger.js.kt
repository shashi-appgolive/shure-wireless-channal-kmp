package com.shure.wireless.channels.core.common

actual object Logger {
    actual var isDebugEnabled: Boolean = true

    actual fun d(tag: String, message: String) {
        if (isDebugEnabled) {
            AppLogStore.record(LogLevel.DEBUG, tag, message)
            console.log("[DEBUG][$tag] $message")
        }
    }

    actual fun i(tag: String, message: String) {
        AppLogStore.record(LogLevel.INFO, tag, message)
        console.info("[INFO][$tag] $message")
    }

    actual fun w(tag: String, message: String) {
        AppLogStore.record(LogLevel.WARN, tag, message)
        console.warn("[WARN][$tag] $message")
    }

    actual fun e(tag: String, message: String, throwable: Throwable?) {
        AppLogStore.record(LogLevel.ERROR, tag, message, throwable)
        console.error("[ERROR][$tag] $message", throwable)
    }
}
