package com.shure.wireless.channels.devices.data.graphql

import com.shure.wireless.channels.devices.domain.model.StoredDevice
import kotlinx.serialization.Serializable

// Must match the operation name declared in DiscoverDevicesQuery below.
internal const val DiscoverDevicesOperationName = "DiscoveredDevices"

internal const val DiscoverDevicesQuery = """
    query DiscoveredDevices {
        discoveredDevices {
            id
            virtual
            status
            compatibility
            manufacturer
            hardwareId
        }
    }
"""

@Serializable
internal data class DiscoverDevicesData(
    val discoveredDevices: List<DiscoveredDeviceNode> = emptyList(),
)

@Serializable
internal data class DiscoveredDeviceNode(
    val id: String,
    val hardwareId: String? = null,
    val status: String? = null,
    val virtual: Boolean? = null,
    val compatibility: String? = null,
    val manufacturer: String? = null,
)

internal fun DiscoverDevicesData.toStoredDevices(): List<StoredDevice> =
    discoveredDevices.map { device -> device.toStoredDevice() }

private fun DiscoveredDeviceNode.toStoredDevice(): StoredDevice =
    StoredDevice(
        id = id,
        name = manufacturer ?: hardwareId ?: id,
        model = compatibility,
        hardwareId = hardwareId,
        status = status,
        category = manufacturer,
        ipAddress = null,
        firmwareVersion = null,
        lastSeenAtEpochMillis = 1L,
    )
