package com.shure.wireless.channels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shure.wireless.channels.ui.components.BackArrowIcon
import com.shure.wireless.channels.ui.theme.ShureColors

@Composable
internal fun ChannelDetailToolbar(
    channelName: String,
    onBack: () -> Unit,
    onEditDeviceName: () -> Unit,
    onRefresh: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth().background(Color(0xFF14191B)).padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(BackArrowIcon, contentDescription = "Back to inventory", tint = ShureColors.Green)
        }
        Text(channelName, modifier = Modifier.weight(1f).padding(start = 6.dp), color = Color.White, fontSize = 20.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Box {
            IconButton(onClick = { menuExpanded = true }, modifier = Modifier.semantics { contentDescription = "Channel menu" }) {
                Text("⋮", color = ShureColors.Green, fontSize = 26.sp)
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                DropdownMenuItem(text = { Text("Rename receiver") }, onClick = { menuExpanded = false; onEditDeviceName() })
                DropdownMenuItem(text = { Text("Refresh channel") }, onClick = { menuExpanded = false; onRefresh() })
            }
        }
    }
}
