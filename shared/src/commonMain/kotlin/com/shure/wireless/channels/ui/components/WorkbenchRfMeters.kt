package com.shure.wireless.channels.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shure.wireless.channels.devices.domain.model.RfMeterChange
import com.shure.wireless.channels.ui.components.TinyRfMeter

@Composable
fun WorkbenchRfMeters(
    meter: RfMeterChange?,
    modifier: Modifier = Modifier,
) {
    Column(modifier.width(72.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        TinyRfMeter(meter?.antennas?.firstOrNull { it.antenna.endsWith("A") }?.level)
        TinyRfMeter(meter?.antennas?.firstOrNull { it.antenna.endsWith("B") }?.level)
    }
}
