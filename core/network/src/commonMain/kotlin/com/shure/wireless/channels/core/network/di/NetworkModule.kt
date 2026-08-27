package com.shure.wireless.channels.core.network.di

import com.shure.wireless.channels.core.network.NetworkApiClient
import com.shure.wireless.channels.core.network.NetworkModuleConfig
import com.shure.wireless.channels.core.network.createNetworkHttpClient
import com.shure.wireless.channels.core.network.createWebSocketHttpClient
import com.shure.wireless.channels.core.network.graphql.GraphQlClient
import com.shure.wireless.channels.core.network.websocket.WebSocketClient
import com.shure.wireless.channels.core.network.websocket.WebSocketController
import com.shure.wireless.channels.core.network.websocket.WebSocketEventListener
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.dsl.onClose

fun networkModule(config: NetworkModuleConfig): Module = module {
    single<String>(RestBaseUrlQualifier) {
        config.rest.normalizedBaseUrl
    }

    single<String>(GraphQlBaseUrlQualifier) {
        config.graphQl.normalizedBaseUrl
    }

    single<String>(WebSocketBaseUrlQualifier) {
        config.webSocket.normalizedBaseUrl
    }

    single<HttpClient>(RestHttpClientQualifier, createdAtStart = true) {
        createNetworkHttpClient(config.rest)
    } onClose { client ->
        client?.close()
    }

    single<HttpClient>(GraphQlHttpClientQualifier, createdAtStart = true) {
        createNetworkHttpClient(config.graphQl)
    } onClose { client ->
        client?.close()
    }

    single<HttpClient>(WebSocketHttpClientQualifier, createdAtStart = true) {
        createWebSocketHttpClient(config.webSocket)
    } onClose { client ->
        client?.close()
    }

    single<NetworkApiClient>(RestApiClientQualifier) {
        NetworkApiClient(
            httpClient = get(RestHttpClientQualifier),
            baseUrl = get(RestBaseUrlQualifier),
        )
    }

    single<GraphQlClient>(GraphQlApiClientQualifier) {
        GraphQlClient(
            httpClient = get(GraphQlHttpClientQualifier),
            endpoint = get(GraphQlBaseUrlQualifier),
        )
    }

    single<WebSocketClient>(WebSocketApiClientQualifier) {
        WebSocketClient(
            httpClient = get(WebSocketHttpClientQualifier),
            baseUrl = get(WebSocketBaseUrlQualifier),
        )
    }

    factory<WebSocketController> { parameters ->
        WebSocketController(
            client = get(WebSocketApiClientQualifier),
            scope = parameters.get<CoroutineScope>(),
            listener = parameters.get<WebSocketEventListener>(),
        )
    }
}
