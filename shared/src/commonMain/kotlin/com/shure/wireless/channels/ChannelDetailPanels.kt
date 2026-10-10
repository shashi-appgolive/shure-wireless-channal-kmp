package com.shure.wireless.channels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shure.wireless.channels.devices.domain.model.AudioChannel
import com.shure.wireless.channels.devices.domain.model.AudioMeterChange
import com.shure.wireless.channels.devices.domain.model.DiscoveredDevice
import com.shure.wireless.channels.devices.domain.model.RfChannel
import com.shure.wireless.channels.devices.domain.model.RfMeterChange
import com.shure.wireless.channels.ui.theme.ShureColors

private val DetailSurface = Color(0xFF1C1C1E)
private val DividerColor = Color(0xFF363B3B)

@Composable
internal fun RfChannelPanel(
    channel: RfChannel,
    device: DiscoveredDevice,
    meter: RfMeterChange?,
    onRfSettingsClick: () -> Unit,
) {
    val frequency = channel.tuning?.frequency?.let(::formatWorkbenchFrequency) ?: "—"
    val antennaA = meter?.antennas?.firstOrNull { it.antenna.endsWith("A") }?.level
    val antennaB = meter?.antennas?.firstOrNull { it.antenna.endsWith("B") }?.level
    val overloaded = isRfMeterFull(antennaA) || isRfMeterFull(antennaB)
    Column(Modifier.fillMaxWidth().background(DetailSurface).padding(start = 20.dp, end = 20.dp, top = 12.dp)) {
        Column(Modifier.fillMaxWidth()
            .then(if (overloaded) Modifier.border(1.dp, RfOverload, RoundedCornerShape(4.dp)) else Modifier)
            .padding(2.dp)) {
            RfAntennaSignal("A", antennaA)
            RfAntennaSignal("B", antennaB)
        }
        Spacer(Modifier.height(12.dp))
        DetailDivider()
        Row(
            Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Frequency", color = Color.White, fontSize = 18.sp)
            Text(frequency, color = ShureColors.TextMuted, fontSize = 18.sp)
        }
        val band = channel.assignedRfBand?.band ?: device.features.rfBand
//        if (band != null) {
//            DetailDivider()
//            Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
//                Text("RF Band", color = Color.White, fontSize = 16.sp)
//                Text(band, color = ShureColors.TextMuted, fontSize = 16.sp)
//            }
//        }
        DetailDivider()
        Row(
            Modifier.fillMaxWidth().clickable(onClick = onRfSettingsClick).padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Radio Frequency Settings", Modifier.weight(1f), color = Color.White, fontSize = 18.sp)
            Text("›", color = ShureColors.TextMuted, fontSize = 28.sp)
        }
    }
}

@Composable
private fun RfAntennaSignal(label: String, level: Double?) {
    Row(Modifier.fillMaxWidth().height(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.width(22.dp), color = Color.White, fontSize = 10.sp, lineHeight = 10.sp)
        TinyRfMeter(level, Modifier.weight(1f), height = 4.dp, showBorder = false)
    }
}

@Composable
internal fun AudioChannelPanel(
    channel: AudioChannel,
    model: String,
    meter: AudioMeterChange?,
    meterProgress: (Double, String) -> Float,
    isUpdatingAudioGain: Boolean,
    onUpdateAudioGain: (String, Int) -> Unit,
    onAudioSettingsClick: () -> Unit,
) {
    val gain = (channel.features.gain ?: 0.0).toInt()
    val audioProgress = meter?.rmsLevel?.let { meterProgress(it, model) } ?: 0f
    Column(Modifier.fillMaxWidth().background(DetailSurface).padding(start = 20.dp, end = 20.dp, top = 14.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("A", Modifier.width(22.dp), color = ShureColors.TextMuted, fontSize = 12.sp)
            AudioMeter(audioProgress, Modifier.weight(1f), height = 5.dp)
        }
        Spacer(Modifier.height(12.dp))
        DetailDivider()
        Row(Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Audio Gain", Modifier.weight(1f), color = Color.White, fontSize = 18.sp)
            Text("${if (gain > 0) "+" else ""}$gain dB", color = Color.White, fontSize = 14.sp)
            Spacer(Modifier.width(4.dp))
            GainButton("−", enabled = !isUpdatingAudioGain) { onUpdateAudioGain(channel.id, gain - 1) }
            Spacer(Modifier.width(4.dp))
            GainButton("+", enabled = !isUpdatingAudioGain) { onUpdateAudioGain(channel.id, gain + 1) }
        }
        Spacer(Modifier.height(10.dp))
        DetailDivider()
        Row(
            Modifier.fillMaxWidth().clickable(onClick = onAudioSettingsClick).padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Audio Settings", Modifier.weight(1f), color = Color.White, fontSize = 18.sp)
            Text("›", color = ShureColors.TextMuted, fontSize = 28.sp)
        }
    }
}

@Composable
private fun GainButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(width = 56.dp, height = 36.dp)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(width = 52.dp, height = 28.dp).background(Color(0xFF303434)), contentAlignment = Alignment.Center) {
            Text(label, color = if (enabled) ShureColors.Green else ShureColors.TextMuted, fontSize = 18.sp)
        }
    }
}


@Composable
private fun DetailDivider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(DividerColor))
}
