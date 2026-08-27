package com.shure.wireless.channels.core.network.websocket

sealed interface WebSocketEvent {
    data object Connecting : WebSocketEvent
    data object Connected : WebSocketEvent
    data class TextMessage(val text: String) : WebSocketEvent
    data class BinaryMessage(val bytes: ByteArray) : WebSocketEvent
    data object Closing : WebSocketEvent
    data class Closed(
        val code: Short?,
        val reason: String?,
    ) : WebSocketEvent

    data class Failure(val exception: Throwable) : WebSocketEvent
}

fun interface WebSocketEventListener {
    fun onEvent(event: WebSocketEvent)
}

abstract class WebSocketCallback : WebSocketEventListener {
    final override fun onEvent(event: WebSocketEvent) {
        when (event) {
            WebSocketEvent.Connecting -> onConnecting()
            WebSocketEvent.Connected -> onConnected()
            is WebSocketEvent.TextMessage -> onTextMessage(event.text)
            is WebSocketEvent.BinaryMessage -> onBinaryMessage(event.bytes)
            WebSocketEvent.Closing -> onClosing()
            is WebSocketEvent.Closed -> onClosed(event.code, event.reason)
            is WebSocketEvent.Failure -> onFailure(event.exception)
        }
    }

    open fun onConnecting() = Unit
    open fun onConnected() = Unit
    open fun onTextMessage(text: String) = Unit
    open fun onBinaryMessage(bytes: ByteArray) = Unit
    open fun onClosing() = Unit
    open fun onClosed(code: Short?, reason: String?) = Unit
    open fun onFailure(exception: Throwable) = Unit
}
