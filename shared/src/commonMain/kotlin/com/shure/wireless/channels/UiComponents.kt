package com.shure.wireless.channels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shure.wireless.channels.ui.theme.ShureColors

@Composable
internal fun ErrorBanner(message: String, onDismiss: () -> Unit, onRetry: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().background(ShureColors.Error.copy(alpha = 0.12f), RoundedCornerShape(10.dp)).padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(message, color = ShureColors.Error, modifier = Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            onRetry?.let { retry ->
                Button(
                    onClick = retry,
                    colors = ButtonDefaults.buttonColors(containerColor = ShureColors.Green, contentColor = Color.Black),
                ) { Text("Try again") }
            }
            OutlinedButton(onClick = onDismiss) { Text("Dismiss") }
        }
    }
}
