package com.shure.wireless.channels.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shure.wireless.channels.devices.domain.model.AudioMeterChange
import com.shure.wireless.channels.devices.domain.model.DiscoveredDevice
import com.shure.wireless.channels.devices.domain.model.RfMeterChange
import com.shure.wireless.channels.ui.workbench.WorkbenchChannelUiModel
import com.shure.wireless.channels.ui.theme.ShureDimens

@Composable
fun WorkbenchChannelRow(
    device: DiscoveredDevice,
    channel: WorkbenchChannelUiModel,
    audioMeters: Map<String, AudioMeterChange>,
    rfMeters: Map<String, RfMeterChange>,
    meterProgress: (Double, String) -> Float,
    mutedColor: Color,
    accentColor: Color,
    onClick: () -> Unit,
) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(ShureDimens.ChannelNumberSize).background(Color(0xFF202124), CircleShape), contentAlignment = Alignment.Center) {
            Text("${channel.channelIndex + 1}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.size(ShureDimens.SmallSpacing))
        Column(Modifier.weight(1f)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(channel.channelName.take(10) + if (channel.channelName.length > 10) "…" else "", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.weight(1f))
                WorkbenchRfMeters(channel.rfChannelId?.let { rfMeters[it] })
            }
            Spacer(Modifier.height(4.dp))
            AudioMeter(audioMeters[channel.channelId]?.rmsLevel?.let { meterProgress(it, channel.deviceType) } ?: 0f)
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(buildAnnotatedString {
                    withStyle(SpanStyle(color = mutedColor)) { append("${channel.model} · ${channel.rfBand} · ") }
                    withStyle(SpanStyle(color = accentColor)) { append(channel.deviceName) }
                }, fontSize = 10.sp, maxLines = 1, modifier = Modifier.weight(1f))
                Text(channel.frequency, color = Color.White, fontSize = 10.sp, maxLines = 1)
            }
        }
    }
}
