package com.shure.wireless.channels.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.shure.wireless.channels.devices.domain.model.RfChannel
import com.shure.wireless.channels.devices.domain.model.RfMeterChange
import com.shure.wireless.channels.ui.components.TinyRfMeter
import com.shure.wireless.channels.ui.components.formatWorkbenchFrequency

@Composable
fun RfChannelDetails(
    channel: RfChannel,
    meter: RfMeterChange?,
    rfBand: String,
    surfaceColor: Color,
    mutedColor: Color,
) {
    val frequency = channel.tuning?.frequency?.let(::formatWorkbenchFrequency) ?: "—"
    Column(Modifier.fillMaxWidth().background(surfaceColor).padding(horizontal = 24.dp, vertical = 14.dp)) {
        Text("Antenna A", color = mutedColor, fontSize = 11.sp)
        TinyRfMeter(meter?.antennas?.firstOrNull { it.antenna.endsWith("A") }?.level)
        Spacer(Modifier.height(6.dp))
        Text("Antenna B", color = mutedColor, fontSize = 11.sp)
        TinyRfMeter(meter?.antennas?.firstOrNull { it.antenna.endsWith("B") }?.level)
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Frequency", color = Color.White, fontSize = 18.sp)
            Text(frequency, color = mutedColor, fontSize = 18.sp)
        }
        Text("RF band: $rfBand", color = mutedColor, fontSize = 12.sp)
    }
}
