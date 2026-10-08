package com.shure.wireless.channels.devices.presentation

import com.shure.wireless.channels.devices.domain.model.DiscoveredDevice

data class InventoryUiState(
    val endpoint: String = "",
    val searchQuery: String = "",
    val selectedDeviceId: String? = null,
    val selectedChannelIndex: Int = 0,
    val onlineOnly: Boolean = false,
    val sortAscending: Boolean = true,
    val totalDevices: Int = 0,
    val onlineCount: Int = 0,
    val hasKnownStatuses: Boolean = false,
    val visibleDevices: List<DiscoveredDevice> = emptyList(),
) {
    fun withDevices(devices: List<DiscoveredDevice>): InventoryUiState {
        val query = searchQuery.trim()
        val sorted = devices.asSequence()
            .filter { !onlineOnly || it.isOnline() }
            .filter { device ->
                query.isEmpty() || buildString {
                    append(device.features.name).append(' ')
                    append(device.interfaceInfo?.model).append(' ')
                    append(device.interfaceInfo?.category).append(' ')
                    append(device.interfaceInfo?.type).append(' ')
                    append(device.status).append(' ')
                    append(device.features.rfBand).append(' ')
                    device.features.audioChannels.forEach { append(it.features.name).append(' ') }
                    device.features.rfChannels.forEach {
                        append(it.assignedRfProfile).append(' ')
                        append(it.tuning?.frequency).append(' ')
                    }
                }.contains(query, ignoreCase = true)
            }
            .sortedWith(
                compareBy<DiscoveredDevice> { (it.features.name?.trim()?.takeIf(String::isNotEmpty) ?: it.id).lowercase() }
                    .thenBy { it.id },
            )
            .toList()
        return copy(
            visibleDevices = if (sortAscending) sorted else sorted.asReversed(),
            totalDevices = devices.size,
            onlineCount = devices.count(DiscoveredDevice::isOnline),
            hasKnownStatuses = devices.isNotEmpty() && devices.all {
                it.isOnline() || it.status.equals("OFFLINE", true) || it.status.equals("DISCONNECTED", true)
            },
        )
    }
}

private fun DiscoveredDevice.isOnline(): Boolean =
    status.equals("ONLINE", ignoreCase = true) || status.equals("CONNECTED", ignoreCase = true)
