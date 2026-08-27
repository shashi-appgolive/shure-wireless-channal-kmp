package com.shure.wireless.channels.core.network.websocket

import kotlin.test.Test
import kotlin.test.assertEquals

class WebSocketCallbackTest {
    @Test
    fun dispatchesEventsToOverridableCallbacks() {
        val changes = mutableListOf<String>()
        val callback = object : WebSocketCallback() {
            override fun onConnecting() {
                changes += "connecting"
            }

            override fun onConnected() {
                changes += "connected"
            }

            override fun onTextMessage(text: String) {
                changes += "text:$text"
            }

            override fun onClosed(code: Short?, reason: String?) {
                changes += "closed:$code:$reason"
            }
        }

        callback.onEvent(WebSocketEvent.Connecting)
        callback.onEvent(WebSocketEvent.Connected)
        callback.onEvent(WebSocketEvent.TextMessage("device-updated"))
        callback.onEvent(WebSocketEvent.Closed(1000, "complete"))

        assertEquals(
            listOf(
                "connecting",
                "connected",
                "text:device-updated",
                "closed:1000:complete",
            ),
            changes,
        )
    }
}
