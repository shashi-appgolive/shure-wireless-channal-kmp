package com.shure.wireless.channels.ui.workbench

import com.shure.wireless.channels.devices.domain.model.DiscoveredDevice

data class DeviceDetailsUiModel(
    val deviceId: String,
    val deviceName: String,
    val channelName: String,
    val receiverId: String,
    val receiverModel: String,
    val connectionStatus: String,
    val rfBand: String,
)

fun DiscoveredDevice.toDeviceDetailsUiModel(channelIndex: Int): DeviceDetailsUiModel {
    val rfChannel = features.rfChannels.getOrNull(channelIndex)
    val audioChannel = features.audioChannels.getOrNull(channelIndex)
    return DeviceDetailsUiModel(
        deviceId = id,
        deviceName = features.name ?: id,
        channelName = audioChannel?.features?.name ?: "Channel ${channelIndex + 1}",
        receiverId = features.serialNumber ?: id,
        receiverModel = interfaceInfo?.model ?: "Shure device",
        connectionStatus = status ?: "Network",
        rfBand = rfChannel?.assignedRfBand?.band ?: features.rfBand ?: "—",
    )
}
