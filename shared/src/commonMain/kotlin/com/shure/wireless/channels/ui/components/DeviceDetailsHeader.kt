package com.shure.wireless.channels.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DeviceDetailsHeader(
    channelName: String,
    surfaceColor: androidx.compose.ui.graphics.Color,
    accentColor: androidx.compose.ui.graphics.Color,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onRefresh: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().background(surfaceColor).padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) { Icon(BackArrowIcon, contentDescription = "Back", tint = accentColor) }
        Text(channelName, color = androidx.compose.ui.graphics.Color.White, fontSize = 22.sp, modifier = Modifier.weight(1f))
        IconButton(onClick = onEdit) { Text("✎", color = accentColor, fontSize = 24.sp) }
        IconButton(onClick = onRefresh) { Text("↻", color = accentColor, fontSize = 24.sp) }
    }
}
