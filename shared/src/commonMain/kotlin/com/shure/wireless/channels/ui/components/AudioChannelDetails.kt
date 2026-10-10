package com.shure.wireless.channels.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shure.wireless.channels.devices.domain.model.AudioChannel
import com.shure.wireless.channels.ui.components.AudioMeter

@Composable
fun AudioChannelDetails(
    channel: AudioChannel,
    rmsValue: Double?,
    gain: Int,
    meterProgress: (Double) -> Float,
    surfaceColor: Color,
    accentColor: Color,
    onDecreaseGain: () -> Unit,
    onIncreaseGain: () -> Unit,
    onAudioSettingsClick: () -> Unit,
) {
    Column(Modifier.fillMaxWidth().background(surfaceColor).padding(start = 24.dp, end = 24.dp, top = 14.dp)) {
        Text(channel.features.name ?: channel.id, color = Color.White, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))
        AudioMeter(progress = rmsValue?.let(meterProgress) ?: 0f)
        AudioGainControl(
            gain = gain,
            accentColor = accentColor,
            onDecrease = onDecreaseGain,
            onIncrease = onIncreaseGain,
        )
        Spacer(Modifier.height(10.dp))
        HorizontalDivider(color = Color(0xFF363B3B))
        Row(Modifier.fillMaxWidth().clickable(onClick = onAudioSettingsClick).padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Audio Settings", Modifier.weight(1f), color = Color.White, fontSize = 18.sp)
            Text("›", color = Color(0xFF98A39D), fontSize = 28.sp)
        }
    }
}
