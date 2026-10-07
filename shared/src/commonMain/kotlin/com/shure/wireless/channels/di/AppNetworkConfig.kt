package com.shure.wireless.channels.di

import com.shure.wireless.channels.core.common.Logger
import com.shure.wireless.channels.core.network.NetworkEndpointConfig
import com.shure.wireless.channels.core.network.NetworkLogger
import com.shure.wireless.channels.core.network.NetworkModuleConfig
import com.shure.wireless.channels.core.network.WebSocketEndpointConfig

object AppNetworkEndpoints {
    const val DefaultPort = 11000
}

fun defaultAppNetworkConfig(): NetworkModuleConfig {
    val restBaseUrl = defaultLocalHttpBaseUrl()
    val graphQlBaseUrl = defaultGraphQlBaseUrl()
    val webSocketBaseUrl = defaultGraphQlWebSocketBaseUrl()
    Logger.d(
        TAG,
        "Network config: rest=$restBaseUrl, graphql=$graphQlBaseUrl, websocket=$webSocketBaseUrl",
    )
    return NetworkModuleConfig(
        rest = NetworkEndpointConfig(
            baseUrl = restBaseUrl,
            enableLogging = Logger.isDebugEnabled,
            logger = AppNetworkLogger,
        ),
        graphQl = NetworkEndpointConfig(
            baseUrl = graphQlBaseUrl,
            enableLogging = Logger.isDebugEnabled,
            logger = AppNetworkLogger,
        ),
        webSocket = WebSocketEndpointConfig(
            baseUrl = webSocketBaseUrl,
            enableLogging = Logger.isDebugEnabled,
            logger = AppNetworkLogger,
        ),
    )
}

private object AppNetworkLogger : NetworkLogger {
    override fun log(message: String) {
        Logger.d("KtorNetwork", message)
    }
}

private const val TAG = "AppNetworkConfig"
