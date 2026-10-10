package com.shure.wireless.channels.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DeviceReceiverInfo(
    receiverName: String,
    receiverModel: String,
    channelNumber: Int,
    surfaceColor: Color,
    mutedColor: Color,
    onAllSettingsClick: () -> Unit,
) {
    Column(Modifier.fillMaxWidth().background(surfaceColor).padding(horizontal = 20.dp)) {
        ReceiverInfoRow("Receiver ID", receiverName, mutedColor)
        HorizontalDivider(color = Color(0xFF363B3B))
        ReceiverInfoRow("Receiver Model", receiverModel, mutedColor)
        HorizontalDivider(color = Color(0xFF363B3B))
        ReceiverInfoRow("Channel Number", channelNumber.toString(), mutedColor)
        HorizontalDivider(color = Color(0xFF363B3B))
        Row(
            Modifier.fillMaxWidth().clickable(onClick = onAllSettingsClick).padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("All Receiver Settings", Modifier.weight(1f), color = Color.White, fontSize = 18.sp)
            Text("›", color = mutedColor, fontSize = 28.sp)
        }
    }
}

@Composable
private fun ReceiverInfoRow(label: String, value: String, mutedColor: Color) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), color = Color.White, fontSize = 18.sp)
        Text(value, Modifier.weight(1f), color = mutedColor, fontSize = 18.sp, textAlign = TextAlign.End, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}
