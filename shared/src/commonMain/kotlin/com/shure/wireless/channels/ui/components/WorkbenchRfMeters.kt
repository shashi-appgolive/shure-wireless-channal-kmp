package com.shure.wireless.channels.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shure.wireless.channels.RfOverload
import com.shure.wireless.channels.devices.domain.model.RfMeterChange
import com.shure.wireless.channels.isRfMeterFull

@Composable
fun WorkbenchRfMeters(
    meter: RfMeterChange?,
    modifier: Modifier = Modifier,
) {
    val antennaA = meter?.antennas?.firstOrNull { it.antenna.endsWith("A") }?.level
    val antennaB = meter?.antennas?.firstOrNull { it.antenna.endsWith("B") }?.level
    val overloaded = isRfMeterFull(antennaA) || isRfMeterFull(antennaB)
    Column(modifier.width(72.dp)
        .then(if (overloaded) Modifier.border(1.dp, RfOverload, RoundedCornerShape(4.dp)) else Modifier)
        .padding(2.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        TinyRfMeter(antennaA, showBorder = false)
        TinyRfMeter(antennaB, showBorder = false)
    }
}
