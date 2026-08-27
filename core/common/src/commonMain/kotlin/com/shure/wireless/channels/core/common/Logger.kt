package com.shure.wireless.channels.core.common

expect object Logger {
    var isDebugEnabled: Boolean

    fun d(tag: String, message: String)
    fun i(tag: String, message: String)
    fun w(tag: String, message: String)
    fun e(tag: String, message: String, throwable: Throwable? = null)
}

enum class LogLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR,
}
