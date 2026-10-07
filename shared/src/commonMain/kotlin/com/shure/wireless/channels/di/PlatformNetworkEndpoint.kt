package com.shure.wireless.channels.di

expect fun defaultLocalHostPort(): String

fun defaultLocalHttpBaseUrl(): String = "http://${defaultLocalHostPort()}"

fun defaultLocalWebSocketBaseUrl(): String = "ws://${defaultLocalHostPort()}"

/** Default GraphQL endpoint used by the reference app. */
fun defaultGraphQlBaseUrl(): String = "https://helping-scrimmage-bunkmate.ngrok-free.dev/"
