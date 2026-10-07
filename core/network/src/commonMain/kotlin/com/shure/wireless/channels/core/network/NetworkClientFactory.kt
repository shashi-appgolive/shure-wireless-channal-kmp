package com.shure.wireless.channels.core.network

import com.shure.wireless.channels.core.network.graphql.GraphQlClient
import com.shure.wireless.channels.core.network.websocket.WebSocketClient
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class NetworkClients internal constructor(
    val restHttpClient: HttpClient,
    val graphQlHttpClient: HttpClient?,
    val webSocketHttpClient: HttpClient?,
    val rest: NetworkApiClient,
    val graphQl: GraphQlClient?,
    val webSocket: WebSocketClient?,
) {
    fun close() {
        restHttpClient.close()
        graphQlHttpClient?.close()
        webSocketHttpClient?.close()
    }
}

object NetworkClientFactory {
    fun create(config: NetworkClientConfig): NetworkClients {
        val restEndpoint = config.toEndpoint(config.baseUrl)
        val graphQlEndpoint = config.graphQlUrl
            ?.takeIf(String::isNotBlank)
            ?.let(config::toEndpoint)
        val restHttpClient = createNetworkHttpClient(restEndpoint)
        val graphQlHttpClient = graphQlEndpoint?.let(::createNetworkHttpClient)
        val webSocketEndpoint = config.webSocketUrl
            ?.takeIf(String::isNotBlank)
            ?.let(config::toWebSocketEndpoint)
        val webSocketHttpClient = webSocketEndpoint?.let(::createWebSocketHttpClient)

        return NetworkClients(
            restHttpClient = restHttpClient,
            graphQlHttpClient = graphQlHttpClient,
            webSocketHttpClient = webSocketHttpClient,
            rest = NetworkApiClient(
                httpClient = restHttpClient,
                baseUrl = restEndpoint.normalizedBaseUrl,
            ),
            graphQl = graphQlEndpoint?.let { endpoint ->
                GraphQlClient(
                    httpClient = requireNotNull(graphQlHttpClient),
                    endpoint = endpoint.normalizedBaseUrl,
                )
            },
            webSocket = webSocketEndpoint?.let { endpoint ->
                WebSocketClient(
                    httpClient = requireNotNull(webSocketHttpClient),
                    baseUrl = endpoint.normalizedBaseUrl,
                )
            },
        )
    }
}

internal fun createNetworkHttpClient(config: NetworkEndpointConfig): HttpClient {
    val json = createNetworkJson()

    return createPlatformHttpClient {
        expectSuccess = false

        install(ContentNegotiation) {
            json(json)
        }

        install(HttpTimeout) {
            requestTimeoutMillis = config.requestTimeoutMillis
            connectTimeoutMillis = config.connectTimeoutMillis
            socketTimeoutMillis = config.socketTimeoutMillis
        }

        defaultRequest {
            config.defaultHeaders.forEach { (name, value) ->
                headers.append(name, value)
            }
        }

        if (config.enableLogging) {
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) = config.logger.log(message)
                }
                level = LogLevel.ALL
                sanitizeHeader { header ->
                    header.equals(HttpHeaders.Authorization, ignoreCase = true) ||
                        header.equals(HttpHeaders.Cookie, ignoreCase = true)
                }
            }
        }

        HttpResponseValidator {
            validateResponse { response ->
                if (response.status.value >= 400) {
                    throw response.toNetworkException()
                }
            }
        }
    }
}

internal fun createWebSocketHttpClient(config: WebSocketEndpointConfig): HttpClient {
    val json = createNetworkJson()

    return createPlatformHttpClient {
        install(WebSockets) {
            config.pingIntervalMillis?.let { pingIntervalMillis = it }
            contentConverter = KotlinxWebsocketSerializationConverter(json)
        }

        defaultRequest {
            config.defaultHeaders.forEach { (name, value) ->
                headers.append(name, value)
            }
        }

        if (config.enableLogging) {
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) = config.logger.log(message)
                }
                level = LogLevel.ALL
                sanitizeHeader { header ->
                    header.equals(HttpHeaders.Authorization, ignoreCase = true) ||
                        header.equals(HttpHeaders.Cookie, ignoreCase = true)
                }
            }
        }
    }
}

private fun createNetworkJson(): Json = Json {
    ignoreUnknownKeys = true
    isLenient = false
    explicitNulls = false
    encodeDefaults = true
}

private fun NetworkClientConfig.toEndpoint(url: String): NetworkEndpointConfig =
    NetworkEndpointConfig(
        baseUrl = url,
        defaultHeaders = defaultHeaders,
        requestTimeoutMillis = requestTimeoutMillis,
        connectTimeoutMillis = connectTimeoutMillis,
        socketTimeoutMillis = socketTimeoutMillis,
        enableLogging = enableLogging,
        logger = logger,
    )

private fun NetworkClientConfig.toWebSocketEndpoint(url: String): WebSocketEndpointConfig =
    WebSocketEndpointConfig(
        baseUrl = url,
        defaultHeaders = defaultHeaders,
        pingIntervalMillis = webSocketPingIntervalMillis,
        maxFrameSize = webSocketMaxFrameSize,
        enableLogging = enableLogging,
        logger = logger,
    )
