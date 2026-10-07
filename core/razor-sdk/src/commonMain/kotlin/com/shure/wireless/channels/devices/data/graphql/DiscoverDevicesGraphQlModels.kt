package com.shure.wireless.channels.devices.data.graphql

import com.shure.wireless.channels.devices.domain.model.StoredDevice
import kotlinx.serialization.Serializable

internal const val DiscoverDevicesOperationName = "DiscoverDevices"

internal const val DiscoverDevicesQuery = """
    query DiscoverDevices {
        discoveredDevices {
            id
            description { interface { type model category } }
            features { logicMute { muted } }
            protocol { address type }
            hardwareId
            interface { model type category }
            status
            virtual
            advertisedInterface { category model type }
            compatibility
        }
    }
"""

@Serializable
internal data class DiscoverDevicesData(
    val discoveredDevices: List<DiscoveredDeviceNode> = emptyList(),
)

internal const val DeviceModelsOperationName = "GetDeviceModels"
internal const val DeviceModelsQuery = """
    query GetDeviceModels {
        deviceModels
    }
"""

@Serializable
internal data class DeviceModelsData(
    val deviceModels: List<String> = emptyList(),
)

@Serializable
internal data class DiscoveredDeviceNode(
    val id: String,
    val description: DeviceDescription? = null,
    val features: DeviceFeatures? = null,
    val protocol: DeviceProtocol? = null,
    val hardwareId: String? = null,
    val `interface`: DeviceInterface? = null,
    val status: String? = null,
    val virtual: Boolean? = null,
    val advertisedInterface: DeviceInterface? = null,
    val compatibility: String? = null,
)

@Serializable internal data class DeviceDescription(val `interface`: DeviceInterface? = null)
@Serializable internal data class DeviceFeatures(val logicMute: LogicMute? = null)
@Serializable internal data class LogicMute(val muted: Boolean? = null)
@Serializable internal data class DeviceProtocol(val address: String? = null, val type: String? = null)
@Serializable internal data class DeviceInterface(
    val model: String? = null,
    val type: String? = null,
    val category: String? = null,
)
internal fun DiscoverDevicesData.toStoredDevices(): List<StoredDevice> =
    discoveredDevices.map { device -> device.toStoredDevice() }

private fun DiscoveredDeviceNode.toStoredDevice(): StoredDevice =
    StoredDevice(
        id = id,
        name = `interface`?.model ?: description?.`interface`?.model ?: hardwareId ?: id,
        model = `interface`?.model ?: description?.`interface`?.model,
        hardwareId = hardwareId,
        status = status,
        category = `interface`?.category ?: description?.`interface`?.category,
        ipAddress = protocol?.address,
        firmwareVersion = null,
        lastSeenAtEpochMillis = 1L,
    )
