package com.shure.wireless.channels.core.network.websocket

import com.shure.wireless.channels.core.network.WebSocketEndpointConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class WebSocketClientTest {
    @Test
    fun resolvesRelativeAndAbsoluteWebSocketUrls() {
        val client = WebSocketClient(
            httpClient = HttpClient(MockEngine { respondOk() }),
            baseUrl = "wss://events.example.test/socket/",
        )

        try {
            assertEquals("wss://events.example.test/socket", client.resolve(""))
            assertEquals("wss://events.example.test/socket/devices", client.resolve("/devices"))
            assertEquals("ws://localhost:8080/echo", client.resolve("ws://localhost:8080/echo"))
        } finally {
            client.httpClient.close()
        }
    }

    @Test
    fun rejectsNonWebSocketEndpoint() {
        assertFailsWith<IllegalArgumentException> {
            WebSocketEndpointConfig("https://events.example.test")
        }
    }
}
