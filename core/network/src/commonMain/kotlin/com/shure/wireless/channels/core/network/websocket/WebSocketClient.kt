package com.shure.wireless.channels.core.network.websocket

import com.shure.wireless.channels.core.network.ApiResult
import com.shure.wireless.channels.core.network.safeNetworkCall
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.header

class WebSocketClient(
    @PublishedApi internal val httpClient: HttpClient,
    baseUrl: String,
) {
    val baseUrl: String = baseUrl.trimEnd('/')

    suspend fun connect(
        path: String = "",
        headers: Map<String, String> = emptyMap(),
        block: suspend DefaultClientWebSocketSession.() -> Unit,
    ): ApiResult<Unit> = safeNetworkCall {
        httpClient.webSocket(
            urlString = resolve(path),
            request = {
                headers.forEach { (name, value) -> header(name, value) }
            },
            block = block,
        )
        ApiResult.Success(Unit, WEB_SOCKET_SWITCHING_PROTOCOLS_STATUS)
    }

    suspend fun openSession(
        path: String = "",
        headers: Map<String, String> = emptyMap(),
    ): ApiResult<DefaultClientWebSocketSession> = safeNetworkCall {
        val session = httpClient.webSocketSession(resolve(path)) {
            headers.forEach { (name, value) -> header(name, value) }
        }
        ApiResult.Success(session, WEB_SOCKET_SWITCHING_PROTOCOLS_STATUS)
    }

    internal fun resolve(path: String): String =
        if (path.startsWith("ws://", ignoreCase = true) ||
            path.startsWith("wss://", ignoreCase = true)
        ) {
            path
        } else if (path.isBlank()) {
            baseUrl
        } else {
            "$baseUrl/${path.trimStart('/')}"
        }

    private companion object {
        const val WEB_SOCKET_SWITCHING_PROTOCOLS_STATUS = 101
    }
}
