package com.shure.wireless.channels.di

expect fun defaultLocalHostPort(): String

fun defaultLocalHttpBaseUrl(): String = "http://${defaultLocalHostPort()}"

fun defaultLocalWebSocketBaseUrl(): String = "ws://${defaultLocalHostPort()}"
