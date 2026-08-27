package com.shure.wireless.channels.core.network.websocket

import com.shure.wireless.channels.core.network.ApiResult
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readBytes
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class WebSocketController(
    private val client: WebSocketClient,
    private val scope: CoroutineScope,
    private val listener: WebSocketEventListener,
) {
    private var connectionJob: Job? = null
    private var session: DefaultClientWebSocketSession? = null

    val isConnected: Boolean
        get() = session?.isActive == true

    fun connect(
        path: String = "",
        headers: Map<String, String> = emptyMap(),
    ) {
        if (connectionJob?.isActive == true) return

        connectionJob = scope.launch {
            emit(WebSocketEvent.Connecting)

            when (val result = client.openSession(path, headers)) {
                is ApiResult.Error -> emit(WebSocketEvent.Failure(result.exception))
                is ApiResult.Success -> listen(result.data)
            }
        }
    }

    suspend fun sendText(text: String): Boolean = send(Frame.Text(text))

    suspend fun sendBinary(bytes: ByteArray): Boolean = send(Frame.Binary(fin = true, data = bytes))

    fun disconnect(reason: String = DEFAULT_CLOSE_REASON) {
        val activeSession = session ?: run {
            connectionJob?.cancel()
            return
        }

        scope.launch {
            emit(WebSocketEvent.Closing)
            activeSession.close(CloseReason(CloseReason.Codes.NORMAL, reason))
        }
    }

    fun cancel() {
        connectionJob?.cancel()
        session?.cancel()
        connectionJob = null
        session = null
    }

    private suspend fun listen(activeSession: DefaultClientWebSocketSession) {
        session = activeSession
        emit(WebSocketEvent.Connected)

        try {
            for (frame in activeSession.incoming) {
                when (frame) {
                    is Frame.Text -> emit(WebSocketEvent.TextMessage(frame.readText()))
                    is Frame.Binary -> emit(WebSocketEvent.BinaryMessage(frame.readBytes()))
                    else -> Unit
                }
            }

            val closeReason = activeSession.closeReason.await()
            emit(WebSocketEvent.Closed(closeReason?.code, closeReason?.message))
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Throwable) {
            emit(WebSocketEvent.Failure(exception))
        } finally {
            session = null
            connectionJob = null
        }
    }

    private suspend fun send(frame: Frame): Boolean {
        val activeSession = session ?: return false
        return try {
            activeSession.send(frame)
            true
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Throwable) {
            emit(WebSocketEvent.Failure(exception))
            false
        }
    }

    private fun emit(event: WebSocketEvent) {
        runCatching { listener.onEvent(event) }
    }

    private companion object {
        const val DEFAULT_CLOSE_REASON = "App requested disconnect"
    }
}
