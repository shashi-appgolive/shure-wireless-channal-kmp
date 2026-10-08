package com.shure.wireless.channels.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
) {
    Column(Modifier.fillMaxWidth().background(surfaceColor).padding(horizontal = 24.dp, vertical = 14.dp)) {
        Text(channel.features.name ?: channel.id, color = Color.White, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))
        AudioMeter(progress = rmsValue?.let(meterProgress) ?: 0f)
        AudioGainControl(
            gain = gain,
            accentColor = accentColor,
            onDecrease = onDecreaseGain,
            onIncrease = onIncreaseGain,
        )
    }
}
