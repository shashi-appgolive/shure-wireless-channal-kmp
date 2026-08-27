package com.shure.wireless.channels.core.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

internal actual fun createPlatformHttpClient(
    configure: HttpClientConfig<*>.() -> Unit,
): HttpClient = HttpClient {
    configure()
}
