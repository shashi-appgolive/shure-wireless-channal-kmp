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
import com.shure.wireless.channels.ui.theme.ShureTheme

@Preview
@Composable
private fun ChannelDetailPreview() {
    val device = DiscoveredDevice(
        id = "receiver-1",
        interfaceInfo = DeviceInterfaceInfo(model = "AD4D-A"),
        features = DeviceFeatures(
            name = "AD4D-1",
            audioChannels = listOf(AudioChannel("audio-1", features = AudioChannelFeatures(name = "Channel1", gain = 12.0))),
            rfChannels = listOf(RfChannel("rf-1", tuning = RfTuning(frequency = 507375.0), assignedRfBand = AssignedRfBand(band = "G53"))),
        ),
    )
    ShureTheme {
        ChannelDetailScreen(
            device = device,
            channelIndex = 0,
            audioMeters = mapOf("audio-1" to AudioMeterChange("audio-1", rmsLevel = -12.0)),
            rfMeters = mapOf("rf-1" to RfMeterChange("rf-1", listOf(RfAntennaLevel("ANTENNA_A", -30.0), RfAntennaLevel("ANTENNA_B", -32.0)))),
            meterProgress = { _, _ -> 0.65f },
            onUpdateDeviceName = { _, _ -> },
            onUpdateAudioGain = { _, _ -> },
            isUpdatingAudioGain = false,
            errorMessage = null,
            onClearError = {},
            onRefresh = {},
            onBack = {},
        )
    }
}
