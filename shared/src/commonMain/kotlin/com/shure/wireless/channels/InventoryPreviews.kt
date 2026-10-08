package com.shure.wireless.channels

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.shure.wireless.channels.devices.domain.model.AssignedRfBand
import com.shure.wireless.channels.devices.domain.model.AudioChannel
import com.shure.wireless.channels.devices.domain.model.AudioChannelFeatures
import com.shure.wireless.channels.devices.domain.model.AudioMeterChange
import com.shure.wireless.channels.devices.domain.model.DeviceFeatures
import com.shure.wireless.channels.devices.domain.model.DeviceInterfaceInfo
import com.shure.wireless.channels.devices.domain.model.DiscoveredDevice
import com.shure.wireless.channels.devices.domain.model.RfAntennaLevel
import com.shure.wireless.channels.devices.domain.model.RfChannel
import com.shure.wireless.channels.devices.domain.model.RfMeterChange
import com.shure.wireless.channels.devices.domain.model.RfTuning
import com.shure.wireless.channels.devices.presentation.DeviceUiState
import com.shure.wireless.channels.devices.presentation.InventoryUiState

@Preview
@Composable
private fun InventoryScreenPreview() {
    val devices = listOf(
        DiscoveredDevice(
            id = "receiver-1",
            status = "ONLINE",
            interfaceInfo = DeviceInterfaceInfo(model = "AD4D-A"),
            features = DeviceFeatures(
                name = "AD4D-2",
                audioChannels = listOf(AudioChannel("audio-1", features = AudioChannelFeatures(name = "Channel1"))),
                rfChannels = listOf(RfChannel("rf-1", tuning = RfTuning(frequency = 569300.0), assignedRfBand = AssignedRfBand(band = "G57"))),
            ),
        ),
    )
    InventoryScreen(
        state = DeviceUiState(
            discoveredConnections = devices,
            inventory = InventoryUiState(endpoint = "http://localhost").withDevices(devices),
            audioMeters = mapOf("audio-1" to AudioMeterChange("audio-1", rmsLevel = -30.0)),
            rfMeters = mapOf("rf-1" to RfMeterChange("rf-1", listOf(RfAntennaLevel("ANTENNA_A", -50.0), RfAntennaLevel("ANTENNA_B", -45.0)))),
        ),
        onDiscover = {},
        onEndpointChange = {},
        onSearchChange = {},
        onToggleOnlineFilter = {},
        onToggleSortOrder = {},
        onSelectChannel = { _, _ -> },
        onCloseChannel = {},
        onUpdateDeviceName = { _, _ -> },
        onUpdateAudioGain = { _, _ -> },
        meterProgress = { _, _ -> 0.65f },
        onClearError = {},
    )
}
