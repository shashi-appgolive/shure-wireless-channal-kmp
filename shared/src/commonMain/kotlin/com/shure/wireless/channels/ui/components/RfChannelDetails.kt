package com.shure.wireless.channels.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shure.wireless.channels.devices.domain.model.RfChannel
import com.shure.wireless.channels.devices.domain.model.RfMeterChange
import com.shure.wireless.channels.RfOverload
import com.shure.wireless.channels.isRfMeterFull
import com.shure.wireless.channels.ui.components.TinyRfMeter
import com.shure.wireless.channels.ui.components.formatWorkbenchFrequency

@Composable
fun RfChannelDetails(
    channel: RfChannel,
    meter: RfMeterChange?,
    rfBand: String,
    surfaceColor: Color,
    mutedColor: Color,
    onRfSettingsClick: () -> Unit,
) {
    val frequency = channel.tuning?.frequency?.let(::formatWorkbenchFrequency) ?: "—"
    val antennaA = meter?.antennas?.firstOrNull { it.antenna.endsWith("A") }?.level
    val antennaB = meter?.antennas?.firstOrNull { it.antenna.endsWith("B") }?.level
    val overloaded = isRfMeterFull(antennaA) || isRfMeterFull(antennaB)
    Column(Modifier.fillMaxWidth().background(surfaceColor).padding(start = 24.dp, end = 24.dp, top = 14.dp)) {
        Column(Modifier.fillMaxWidth()
            .then(if (overloaded) Modifier.border(1.dp, RfOverload, RoundedCornerShape(4.dp)) else Modifier)
            .padding(2.dp)) {
            Text("Antenna A", color = mutedColor, fontSize = 11.sp)
            TinyRfMeter(antennaA, showBorder = false)
            Spacer(Modifier.height(6.dp))
            Text("Antenna B", color = mutedColor, fontSize = 11.sp)
            TinyRfMeter(antennaB, showBorder = false)
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Frequency", color = Color.White, fontSize = 18.sp)
            Text(frequency, color = mutedColor, fontSize = 18.sp)
        }
        Text("Band: $rfBand", color = mutedColor, fontSize = 12.sp)
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier.fillMaxWidth().clickable(onClick = onRfSettingsClick).padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Radio Frequency Settings", Modifier.weight(1f), color = Color.White, fontSize = 18.sp)
            Text("›", color = mutedColor, fontSize = 28.sp)
        }
    }
}
