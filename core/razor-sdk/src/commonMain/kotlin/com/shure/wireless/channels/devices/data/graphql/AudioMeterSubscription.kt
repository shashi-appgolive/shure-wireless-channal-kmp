package com.shure.wireless.channels.devices.data.graphql

import com.shure.wireless.channels.core.network.websocket.WebSocketClient
import com.shure.wireless.channels.devices.domain.model.AudioMeterChange
import com.shure.wireless.channels.devices.domain.model.MeterChangeType
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

private val AudioMeterSubscriptionQuery = """
subscription AudioMeterSubscription(${ '$' }channelId: ID!, ${ '$' }types: [MeterChangeType!]!, ${ '$' }updateRate: Duration!) {
  meterChanges(id: ${ '$' }channelId, types: ${ '$' }types, updateRate: ${ '$' }updateRate) {
    ... on AudioChannelMeterChange {
      id
      features { peak { level } rms { level } }
    }
  }
}
"""

class AudioMeterSubscription(
    private val webSocketClient: WebSocketClient,
) {
    fun observe(
        channelId: String,
        types: List<MeterChangeType> = listOf(MeterChangeType.AUDIO_CHANNEL_CLIP, MeterChangeType.AUDIO_CHANNEL_PEAK, MeterChangeType.AUDIO_CHANNEL_RMS),
        updateRate: String = "PT0.1S",
    ): Flow<AudioMeterChange> = callbackFlow {
        webSocketClient.connect(
            headers = mapOf("Sec-WebSocket-Protocol" to "graphql-transport-ws"),
        ) {
            send(Frame.Text("{\"type\":\"connection_init\",\"payload\":{}}"))
            val ack = incoming.receive() as? Frame.Text
            if (!ack?.readText().orEmpty().contains("connection_ack")) {
                close(IllegalStateException("GraphQL WebSocket connection was not acknowledged"))
                return@connect
            }
            val variables = buildJsonObject {
                put("channelId", JsonPrimitive(channelId))
                put("types", kotlinx.serialization.json.buildJsonArray { types.forEach { add(JsonPrimitive(it.name)) } })
                put("updateRate", JsonPrimitive(updateRate))
            }
            val subscribe = buildJsonObject {
                put("id", "audio-meter-$channelId")
                put("type", "subscribe")
                put("payload", buildJsonObject {
                    put("query", AudioMeterSubscriptionQuery)
                    put("variables", variables)
                })
            }
            send(Frame.Text(Json.encodeToString(JsonObject.serializer(), subscribe)))
            for (frame in incoming) {
                if (frame is Frame.Text) parseMeter(frame.readText())?.let { trySend(it) }
            }
        }
        awaitClose { }
    }

    private fun parseMeter(text: String): AudioMeterChange? = runCatching {
        val root = Json.parseToJsonElement(text).jsonObject
        val payload = root["payload"]?.jsonObject ?: return null
        val data = payload["data"]?.jsonObject ?: return null
        val change = data["meterChanges"]?.jsonObject ?: return null
        val features = change["features"]?.jsonObject
        AudioMeterChange(
            id = change["id"]?.jsonPrimitive?.content ?: return null,
            peakLevel = features?.get("peak")?.jsonObject?.get("level")?.jsonPrimitive?.content?.toDoubleOrNull(),
            rmsLevel = features?.get("rms")?.jsonObject?.get("level")?.jsonPrimitive?.content?.toDoubleOrNull(),
        )
    }.getOrNull()
}
