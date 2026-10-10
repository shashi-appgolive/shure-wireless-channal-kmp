package com.shure.wireless.channels.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AudioGainControl(
    gain: Int,
    accentColor: Color,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    enabled: Boolean = true,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Audio Gain", color = Color.White, fontSize = 18.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${if (gain > 0) "+" else ""}$gain dB", color = Color.White, fontSize = 16.sp)
            Spacer(Modifier.width(14.dp))
            CompactGainButton("−", accentColor, enabled, onDecrease)
            Spacer(Modifier.width(12.dp))
            CompactGainButton("+", accentColor, enabled, onIncrease)
        }
    }
}

@Composable
private fun CompactGainButton(label: String, accentColor: Color, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.size(width = 56.dp, height = 36.dp).clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(width = 52.dp, height = 28.dp).background(Color(0xFF303434)), contentAlignment = Alignment.Center) {
            Text(label, color = if (enabled) accentColor else Color.Gray, fontSize = 18.sp)
        }
    }
}
