package com.shure.wireless.channels.core.common

actual object Logger {
    actual var isDebugEnabled: Boolean = true

    actual fun d(tag: String, message: String) {
        if (isDebugEnabled) {
            AppLogStore.record(LogLevel.DEBUG, tag, message)
            log("DEBUG", tag, message)
        }
    }

    actual fun i(tag: String, message: String) {
        AppLogStore.record(LogLevel.INFO, tag, message)
        log("INFO", tag, message)
    }

    actual fun w(tag: String, message: String) {
        AppLogStore.record(LogLevel.WARN, tag, message)
        log("WARN", tag, message)
    }

    actual fun e(tag: String, message: String, throwable: Throwable?) {
        AppLogStore.record(LogLevel.ERROR, tag, message, throwable)
        val details = throwable?.let { "\n${it.stackTraceToString()}" }.orEmpty()
        log("ERROR", tag, message + details)
    }

    /**
     * NSLog is an Objective-C variadic function. Kotlin/Native's interop with
     * variadic C/ObjC functions only reliably supports primitive vararg types;
     * passing a Kotlin String (auto-bridged to NSString) as the vararg argument
     * (NSLog("%@", line)) is a known-fragile path that has been observed to
     * crash with EXC_BAD_ACCESS on both the main thread and background threads.
     * println() goes through the Kotlin/Native runtime's own stdout path with
     * no ObjC vararg bridging involved, and still shows up in the Xcode console.
     */
    private fun log(level: String, tag: String, message: String) {
        println("[$level][$tag] $message")
    }
}
