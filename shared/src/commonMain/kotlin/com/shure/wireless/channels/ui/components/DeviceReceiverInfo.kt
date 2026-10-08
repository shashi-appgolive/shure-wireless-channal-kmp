package com.shure.wireless.channels.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shure.wireless.channels.ui.workbench.DeviceDetailsUiModel

@Composable
fun DeviceReceiverInfo(
    details: DeviceDetailsUiModel,
    displayedDeviceName: String,
    surfaceColor: Color,
) {
    Column(
        Modifier.fillMaxWidth().background(surfaceColor).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Device Name      $displayedDeviceName", color = Color.White)
        Text("Receiver ID       ${details.receiverId}", color = Color.White)
        Text("Receiver Model    ${details.receiverModel}", color = Color.White)
        Text("Connection        ${details.connectionStatus}", color = Color.White)
    }
}
