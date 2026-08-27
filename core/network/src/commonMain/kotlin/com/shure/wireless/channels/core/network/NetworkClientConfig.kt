package com.shure.wireless.channels.core.network

data class NetworkClientConfig(
    val baseUrl: String,
    val graphQlUrl: String? = null,
    val webSocketUrl: String? = null,
    val defaultHeaders: Map<String, String> = emptyMap(),
    val requestTimeoutMillis: Long = 30_000,
    val connectTimeoutMillis: Long = 30_000,
    val socketTimeoutMillis: Long = 30_000,
    val enableLogging: Boolean = false,
    val logger: NetworkLogger = NetworkLogger.None,
    val webSocketPingIntervalMillis: Long? = null,
    val webSocketMaxFrameSize: Long = Long.MAX_VALUE,
) {
    init {
        require(baseUrl.isNotBlank()) { "baseUrl cannot be blank" }
        require(requestTimeoutMillis > 0) { "requestTimeoutMillis must be positive" }
        require(connectTimeoutMillis > 0) { "connectTimeoutMillis must be positive" }
        require(socketTimeoutMillis > 0) { "socketTimeoutMillis must be positive" }
        require(webSocketPingIntervalMillis == null || webSocketPingIntervalMillis > 0) {
            "webSocketPingIntervalMillis must be positive when provided"
        }
        require(webSocketMaxFrameSize > 0) { "webSocketMaxFrameSize must be positive" }
    }
}

data class NetworkEndpointConfig(
    val baseUrl: String,
    val defaultHeaders: Map<String, String> = emptyMap(),
    val requestTimeoutMillis: Long = 30_000,
    val connectTimeoutMillis: Long = 30_000,
    val socketTimeoutMillis: Long = 30_000,
    val enableLogging: Boolean = false,
    val logger: NetworkLogger = NetworkLogger.None,
) {
    init {
        require(baseUrl.isNotBlank()) { "baseUrl cannot be blank" }
        require(requestTimeoutMillis > 0) { "requestTimeoutMillis must be positive" }
        require(connectTimeoutMillis > 0) { "connectTimeoutMillis must be positive" }
        require(socketTimeoutMillis > 0) { "socketTimeoutMillis must be positive" }
    }

    val normalizedBaseUrl: String = baseUrl.trimEnd('/')
}

data class NetworkModuleConfig(
    val rest: NetworkEndpointConfig,
    val graphQl: NetworkEndpointConfig,
    val webSocket: WebSocketEndpointConfig,
)

data class WebSocketEndpointConfig(
    val baseUrl: String,
    val defaultHeaders: Map<String, String> = emptyMap(),
    val pingIntervalMillis: Long? = null,
    val maxFrameSize: Long = Long.MAX_VALUE,
    val enableLogging: Boolean = false,
    val logger: NetworkLogger = NetworkLogger.None,
) {
    init {
        require(baseUrl.isNotBlank()) { "baseUrl cannot be blank" }
        require(
            baseUrl.startsWith("ws://", ignoreCase = true) ||
                baseUrl.startsWith("wss://", ignoreCase = true),
        ) { "WebSocket baseUrl must start with ws:// or wss://" }
        require(pingIntervalMillis == null || pingIntervalMillis > 0) {
            "pingIntervalMillis must be positive when provided"
        }
        require(maxFrameSize > 0) { "maxFrameSize must be positive" }
    }

    val normalizedBaseUrl: String = baseUrl.trimEnd('/')
}

fun interface NetworkLogger {
    fun log(message: String)

    data object None : NetworkLogger {
        override fun log(message: String) = Unit
    }
}
