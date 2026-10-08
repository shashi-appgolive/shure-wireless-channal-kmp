package com.shure.wireless.channels.ui.workbench

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shure.wireless.channels.devices.domain.model.AudioMeterChange
import com.shure.wireless.channels.devices.domain.model.DiscoveredDevice
import com.shure.wireless.channels.devices.domain.model.RfMeterChange
import com.shure.wireless.channels.ui.components.WorkbenchChannelRow
import com.shure.wireless.channels.ui.components.WorkbenchInventorySearch
import com.shure.wireless.channels.ui.components.WorkbenchInventorySummary

@Composable
fun WorkbenchDeviceList(
    devices: List<DiscoveredDevice>,
    audioMeters: Map<String, AudioMeterChange>,
    rfMeters: Map<String, RfMeterChange>,
    meterProgress: (Double, String) -> Float,
    mutedColor: Color,
    accentColor: Color,
    onDeviceClick: (DiscoveredDevice, Int) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredDevices = devices.filter { it.matchesWorkbenchSearch(searchQuery) }
    val totalChannels = filteredDevices.sumOf { it.toWorkbenchChannels().size }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item { WorkbenchInventorySummary(filteredDevices.size, devices.size, totalChannels) }
        item { WorkbenchInventorySearch(searchQuery) { searchQuery = it } }
        items(filteredDevices, key = { it.id }) { device ->
            androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
                device.toWorkbenchChannels().forEach { channel ->
                    WorkbenchChannelRow(
                        device = device,
                        channel = channel,
                        audioMeters = audioMeters,
                        rfMeters = rfMeters,
                        meterProgress = meterProgress,
                        mutedColor = mutedColor,
                        accentColor = accentColor,
                        onClick = { onDeviceClick(device, channel.channelIndex) },
                    )
                }
            }
        }
    }
}
