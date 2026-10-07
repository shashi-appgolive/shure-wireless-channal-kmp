package com.shure.wireless.channels.devices.data.graphql

import com.shure.wireless.channels.core.network.ApiResult
import com.shure.wireless.channels.core.network.graphql.GraphQlClient
import com.shure.wireless.channels.devices.domain.model.StoredDevice
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal const val UpdateDeviceOperationName = "UpdateDevice"
internal const val UpdateAudioChannelGainOperationName = "UpdateAudioChannelGain"
internal const val UpdateAudioChannelGainMutation = """
mutation UpdateAudioChannelGain(${ '$' }updates: [NodeUpdateInput!]!) {
  updateNodes(updates: ${ '$' }updates) {
    ... on AudioChannelOperationResult {
      audioChannel { id features { gain { gain } } }
      error { code message }
    }
  }
}
"""
internal const val UpdateDeviceMutation = """
mutation UpdateDevice(${ '$' }updates: [NodeUpdateInput!]!) {
  updateNodes(updates: ${ '$' }updates) {
    ... on DeviceOperationResult {
      device { id features { name { name } } }
      error { code message }
    }
  }
}
"""

@Serializable internal data class UpdateDeviceData(val updateNodes: List<DeviceOperationResult> = emptyList())
@Serializable internal data class DeviceOperationResult(val device: UpdatedDevice? = null, val error: DeviceOperationError? = null)
@Serializable internal data class UpdatedDevice(val id: String, val features: UpdatedDeviceFeatures? = null)
@Serializable internal data class UpdatedDeviceFeatures(val name: UpdatedName? = null)
@Serializable internal data class UpdatedName(val name: String? = null)
@Serializable internal data class DeviceOperationError(val code: String? = null, val message: String? = null)
@Serializable internal data class UpdateAudioChannelGainData(val updateNodes: List<AudioChannelOperationResult> = emptyList())
@Serializable internal data class AudioChannelOperationResult(val audioChannel: UpdatedAudioChannel? = null, val error: DeviceOperationError? = null)
@Serializable internal data class UpdatedAudioChannel(val id: String, val features: UpdatedAudioFeatures? = null)
@Serializable internal data class UpdatedAudioFeatures(val gain: GainNode? = null)

internal suspend fun GraphQlClient.updateDeviceName(deviceId: String, name: String, endpoint: String = this.endpoint): ApiResult<UpdateDeviceData> {
    val variables = buildJsonObject {
        put("updates", buildJsonArray {
            add(buildJsonObject {
                put("device", buildJsonObject {
                    put("id", deviceId)
                    put("features", buildJsonObject {
                        put("name", buildJsonObject { put("name", name) })
                    })
                })
            })
        })
    }
    return executeData<UpdateDeviceData, kotlinx.serialization.json.JsonObject>(
        UpdateDeviceMutation,
        variables,
        UpdateDeviceOperationName,
        endpoint = endpoint,
    )
}

internal suspend fun GraphQlClient.updateAudioChannelGain(channelId: String, gain: Double, endpoint: String = this.endpoint): ApiResult<UpdateAudioChannelGainData> {
    val variables = buildJsonObject {
        put("updates", buildJsonArray {
            add(buildJsonObject {
                put("audioChannel", buildJsonObject {
                    put("id", channelId)
                    put("features", buildJsonObject { put("gain", buildJsonObject { put("gain", gain) }) })
                })
            })
        })
    }
    return executeData<UpdateAudioChannelGainData, kotlinx.serialization.json.JsonObject>(UpdateAudioChannelGainMutation, variables, UpdateAudioChannelGainOperationName, endpoint = endpoint)
}
