package com.shure.wireless.channels.ui.workbench

import com.shure.wireless.channels.devices.domain.model.DiscoveredDevice
import com.shure.wireless.channels.ui.components.formatWorkbenchFrequency

data class WorkbenchChannelUiModel(
    val channelIndex: Int,
    val channelId: String,
    val channelName: String,
    val deviceId: String,
    val deviceName: String,
    val model: String,
    val deviceType: String,
    val rfBand: String,
    val frequency: String,
    val rfChannelId: String?,
)

fun DiscoveredDevice.toWorkbenchChannels(): List<WorkbenchChannelUiModel> =
    features.rfChannels.mapIndexed { index, rfChannel ->
        val audioChannel = features.audioChannels.getOrNull(index)
        WorkbenchChannelUiModel(
            channelIndex = index,
            channelId = audioChannel?.id ?: rfChannel.id,
            channelName = audioChannel?.features?.name
                ?: rfChannel.assignedRfProfile
                ?: rfChannel.tuning?.channel
                ?: "Channel ${index + 1}",
            deviceId = id,
            deviceName = features.name ?: id,
            model = interfaceInfo?.model ?: "Shure device",
            deviceType = listOf(interfaceInfo?.model, interfaceInfo?.category, interfaceInfo?.type).joinToString(" "),
            rfBand = rfChannel.assignedRfBand?.band ?: features.rfBand ?: "—",
            frequency = rfChannel.tuning?.frequency?.let(::formatWorkbenchFrequency) ?: "—",
            rfChannelId = rfChannel.id,
        )
    }
