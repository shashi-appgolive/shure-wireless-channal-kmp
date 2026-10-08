package com.shure.wireless.channels

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shure.wireless.channels.devices.domain.model.AudioMeterChange
import com.shure.wireless.channels.devices.domain.model.DiscoveredDevice
import com.shure.wireless.channels.devices.domain.model.RfMeterChange

@Composable
internal fun InventoryChannelItem(
    device: DiscoveredDevice,
    channelIndex: Int,
    audioMeter: AudioMeterChange?,
    rfMeter: RfMeterChange?,
    meterProgress: (Double, String) -> Float,
    onClick: () -> Unit,
) {
    val rfChannel = device.features.rfChannels[channelIndex]
    val model = device.interfaceInfo?.model ?: "Shure device"
    val meterDeviceType = listOf(model, device.interfaceInfo?.category, device.interfaceInfo?.type).joinToString(" ")
    val name = device.features.audioChannels.getOrNull(channelIndex)?.features?.name
        ?: rfChannel.assignedRfProfile ?: rfChannel.tuning?.channel ?: "Channel ${channelIndex + 1}"
    val band = rfChannel.assignedRfBand?.band ?: device.features.rfBand ?: "—"
    val frequency = rfChannel.tuning?.frequency?.let(::formatWorkbenchFrequency) ?: "MHz"

    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(54.dp).background(Color(0xFF202020), CircleShape), contentAlignment = Alignment.Center) {
            Text("${channelIndex + 1}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(name, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            AudioMeter(
                progress = audioMeter?.rmsLevel?.let { meterProgress(it, meterDeviceType) } ?: 0f,
                height = 8.dp,
            )
            Spacer(Modifier.height(10.dp))
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val rfWidth = 72.dp.coerceAtMost(maxWidth * 0.25f)
                val frequencyWidth = 68.dp.coerceAtMost(maxWidth * 0.28f)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(model, Modifier.weight(1f), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(band, Modifier.width(32.dp), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    Text(device.features.name ?: device.id, Modifier.weight(1f), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(frequency, Modifier.width(frequencyWidth), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    RfMeterPair(rfMeter, modifier = Modifier.width(rfWidth))
                }
            }
        }
    }
}
