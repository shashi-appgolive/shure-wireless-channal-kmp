package com.shure.wireless.channels.devices.presentation

import com.shure.wireless.channels.core.common.LogEntry
import com.shure.wireless.channels.devices.domain.model.DeviceEvent
import com.shure.wireless.channels.devices.domain.model.DiscoveredDevice
import com.shure.wireless.channels.devices.domain.model.AudioMeterChange
import com.shure.wireless.channels.devices.domain.model.RfMeterChange
import com.shure.wireless.channels.devices.domain.model.StoredDevice

data class DeviceUiState(
    val devices: List<StoredDevice> = emptyList(),
    val deviceModels: List<String> = emptyList(),
    val discoveredConnections: List<DiscoveredDevice> = emptyList(),
    val connectedDevice: StoredDevice? = null,
    val events: List<DeviceEvent> = emptyList(),
    val logs: List<LogEntry> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isConnecting: Boolean = false,
    val isDiscovering: Boolean = false,
    val isLoadingDeviceModels: Boolean = false,
    val isDiscoveringConnections: Boolean = false,
    val listeningChannelIds: Set<String> = emptySet(),
    val audioMeters: Map<String, AudioMeterChange> = emptyMap(),
    val rfMeters: Map<String, RfMeterChange> = emptyMap(),
    val listeningRfChannelIds: Set<String> = emptySet(),
    val isUpdatingDeviceName: Boolean = false,
    val isListening: Boolean = false,
    val errorMessage: String? = null,
)
