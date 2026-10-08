package com.shure.wireless.channels.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

@Composable
fun AudioGainControl(
    gain: Int,
    accentColor: Color,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Audio Gain", color = Color.White, fontSize = 18.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("$gain dB", color = Color.White, fontSize = 16.sp)
            IconButton(onClick = onDecrease) { Text("−", color = accentColor, fontSize = 20.sp) }
            IconButton(onClick = onIncrease) { Text("+", color = accentColor, fontSize = 20.sp) }
        }
    }
}
