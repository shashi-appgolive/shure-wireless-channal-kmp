package com.shure.wireless.channels.core.network.di

import com.shure.wireless.channels.core.network.NetworkApiClient
import com.shure.wireless.channels.core.network.NetworkEndpointConfig
import com.shure.wireless.channels.core.network.NetworkModuleConfig
import com.shure.wireless.channels.core.network.WebSocketEndpointConfig
import com.shure.wireless.channels.core.network.graphql.GraphQlClient
import com.shure.wireless.channels.core.network.websocket.WebSocketClient
import io.ktor.client.HttpClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.koin.dsl.koinApplication

class NetworkModuleTest {
    @Test
    fun providesIndependentRestGraphQlAndWebSocketClients() {
        val application = koinApplication {
            modules(
                networkModule(
                    NetworkModuleConfig(
                        rest = NetworkEndpointConfig("https://rest.example.test/"),
                        graphQl = NetworkEndpointConfig("http://127.0.0.1:11000"),
                        webSocket = WebSocketEndpointConfig("wss://events.example.test/socket/"),
                    ),
                ),
            )
        }

        try {
            val koin = application.koin
            val restHttpClient = koin.get<HttpClient>(RestHttpClientQualifier)
            val graphQlHttpClient = koin.get<HttpClient>(GraphQlHttpClientQualifier)
            val webSocketHttpClient = koin.get<HttpClient>(WebSocketHttpClientQualifier)
            val restApiClient = koin.get<NetworkApiClient>(RestApiClientQualifier)
            val graphQlApiClient = koin.get<GraphQlClient>(GraphQlApiClientQualifier)
            val webSocketApiClient = koin.get<WebSocketClient>(WebSocketApiClientQualifier)

            assertTrue(restHttpClient !== graphQlHttpClient)
            assertTrue(restHttpClient !== webSocketHttpClient)
            assertTrue(graphQlHttpClient !== webSocketHttpClient)
            assertEquals("https://rest.example.test", koin.get<String>(RestBaseUrlQualifier))
            assertEquals(
                "https://graphql.example.test/graphql",
                koin.get<String>(GraphQlBaseUrlQualifier),
            )
            assertEquals(
                "wss://events.example.test/socket",
                koin.get<String>(WebSocketBaseUrlQualifier),
            )
            assertEquals("https://rest.example.test", restApiClient.baseUrl)
            assertEquals("https://graphql.example.test/graphql", graphQlApiClient.endpoint)
            assertEquals("wss://events.example.test/socket", webSocketApiClient.baseUrl)
        } finally {
            application.close()
        }
    }
}
