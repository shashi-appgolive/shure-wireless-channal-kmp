package com.shure.wireless.channels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shure.wireless.channels.devices.domain.model.AudioMeterChange
import com.shure.wireless.channels.devices.domain.model.DiscoveredDevice
import com.shure.wireless.channels.devices.domain.model.RfMeterChange
import com.shure.wireless.channels.ui.theme.ShureColors

@Composable
internal fun InventoryDeviceList(
    devices: List<DiscoveredDevice>,
    audioMeters: Map<String, AudioMeterChange>,
    rfMeters: Map<String, RfMeterChange>,
    meterProgress: (Double, String) -> Float,
    onDeviceClick: (DiscoveredDevice, Int) -> Unit,
) {
    val channelRows = remember(devices) {
        devices.flatMap { device -> device.features.rfChannels.indices.map { index -> device to index } }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (channelRows.isEmpty()) {
            item { Text("No matching devices", modifier = Modifier.padding(top = 20.dp), color = ShureColors.TextMuted) }
        }
        items(channelRows, key = { (device, index) -> "${device.id}:${device.features.rfChannels[index].id}:$index" }) { (device, index) ->
            InventoryChannelItem(
                device = device,
                channelIndex = index,
                audioMeter = device.features.audioChannels.getOrNull(index)?.id?.let(audioMeters::get),
                rfMeter = device.features.rfChannels[index].id.let(rfMeters::get),
                meterProgress = meterProgress,
                onClick = { onDeviceClick(device, index) },
            )
        }
    }
}
