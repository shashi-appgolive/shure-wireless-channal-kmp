package com.shure.wireless.channels.devices.data.graphql

import com.shure.wireless.channels.core.network.websocket.WebSocketClient
import com.shure.wireless.channels.devices.domain.model.MeterChangeType
import com.shure.wireless.channels.devices.domain.model.RfAntennaLevel
import com.shure.wireless.channels.devices.domain.model.RfMeterChange
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

private val RfMeterSubscriptionQuery = """
subscription RfChannelSubscription(${ '$' }channelId: ID!, ${ '$' }types: [MeterChangeType!]!, ${ '$' }updateRate: Duration!) {
  meterChanges(id: ${ '$' }channelId, types: ${ '$' }types, updateRate: ${ '$' }updateRate) {
    ... on RfChannelMeterChange {
      id
      features { signalStrength { antennas { antenna level } } }
    }
  }
}
"""

class RfMeterSubscription(
    private val webSocketClient: WebSocketClient,
) {
    fun observe(
        channelId: String,
        types: List<MeterChangeType> = listOf(MeterChangeType.RF_CHANNEL_SIGNAL_STRENGTH),
        updateRate: String = "PT0.1S",
    ): Flow<RfMeterChange> = callbackFlow {
        webSocketClient.connect(headers = mapOf("Sec-WebSocket-Protocol" to "graphql-transport-ws")) {
            send(Frame.Text("{\"type\":\"connection_init\",\"payload\":{}}"))
            val ack = incoming.receive() as? Frame.Text
            if (!ack?.readText().orEmpty().contains("connection_ack")) {
                close(IllegalStateException("GraphQL WebSocket connection was not acknowledged"))
                return@connect
            }
            val variables = buildJsonObject {
                put("channelId", JsonPrimitive(channelId))
                put("types", buildJsonArray { types.forEach { add(JsonPrimitive(it.name)) } })
                put("updateRate", JsonPrimitive(updateRate))
            }
            val subscribe = buildJsonObject {
                put("id", "rf-meter-$channelId")
                put("type", "subscribe")
                put("payload", buildJsonObject {
                    put("query", RfMeterSubscriptionQuery)
                    put("variables", variables)
                })
            }
            send(Frame.Text(Json.encodeToString(JsonObject.serializer(), subscribe)))
            for (frame in incoming) {
                if (frame is Frame.Text) parse(frame.readText())?.let { trySend(it) }
            }
        }
        awaitClose { }
    }

    private fun parse(text: String): RfMeterChange? = runCatching {
        val root = Json.parseToJsonElement(text).jsonObject
        val change = root["payload"]?.jsonObject?.get("data")?.jsonObject?.get("meterChanges")?.jsonObject
            ?: return null
        val antennas = change["features"]?.jsonObject?.get("signalStrength")?.jsonObject
            ?.get("antennas")?.jsonArray.orEmpty().mapNotNull { item ->
                val antenna = item.jsonObject["antenna"]?.jsonPrimitive?.content ?: return@mapNotNull null
                RfAntennaLevel(antenna, item.jsonObject["level"]?.jsonPrimitive?.content?.toDoubleOrNull())
            }
        RfMeterChange(change["id"]?.jsonPrimitive?.content ?: return null, antennas)
    }.getOrNull()
}
