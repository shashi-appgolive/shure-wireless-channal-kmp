package com.shure.wireless.channels.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

@Composable
fun WorkbenchInventorySummary(deviceCount: Int, totalDeviceCount: Int, channelCount: Int) {
    Row(Modifier.fillMaxWidth()) {
        Text("$deviceCount/$totalDeviceCount devices · $channelCount channels", color = Color.White, fontSize = 16.sp)
    }
}

@Composable
fun WorkbenchInventorySearch(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Find in inventory") },
        singleLine = true,
    )
}
