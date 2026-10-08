package com.shure.wireless.channels

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun InventoryToolbar(
    onlineCount: Int,
    totalDevices: Int,
    hasKnownStatuses: Boolean,
    onlineOnly: Boolean,
    sortAscending: Boolean,
    onToggleOnlineFilter: () -> Unit,
    onToggleSortOrder: () -> Unit,
    onEditEndpoint: () -> Unit,
    onRefresh: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().background(Color(0xFF14191B)).padding(start = 18.dp, end = 16.dp, top = 16.dp, bottom = 10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Inventory", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Box {
                IconButton(onClick = { menuExpanded = true }, modifier = Modifier.semantics { contentDescription = "Inventory menu" }) {
                    Canvas(Modifier.size(24.dp)) {
                        listOf(5f, 12f, 19f).forEach { y ->
                            drawLine(Color.White, Offset(2.dp.toPx(), y.dp.toPx()), Offset(size.width - 2.dp.toPx(), y.dp.toPx()), strokeWidth = 2.dp.toPx())
                        }
                    }
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(text = { Text("Edit base URL") }, onClick = { menuExpanded = false; onEditEndpoint() })
                    DropdownMenuItem(text = { Text("Refresh devices") }, onClick = { menuExpanded = false; onRefresh() })
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 2.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (hasKnownStatuses) "$onlineCount/$totalDevices Online" else "$totalDevices Devices",
                color = Color.White,
                fontSize = 15.sp,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onToggleOnlineFilter,
                    enabled = hasKnownStatuses,
                    modifier = Modifier.semantics { contentDescription = if (onlineOnly) "Show all devices" else "Show online devices" },
                ) {
                    Canvas(Modifier.size(24.dp)) {
                        val color = if (!hasKnownStatuses) Color.Gray else if (onlineOnly) Color(0xFFA6FF00) else Color.White
                        drawLine(color, Offset(2.dp.toPx(), 5.dp.toPx()), Offset(size.width - 2.dp.toPx(), 5.dp.toPx()), strokeWidth = 2.5.dp.toPx())
                        drawLine(color, Offset(6.dp.toPx(), 11.dp.toPx()), Offset(size.width - 6.dp.toPx(), 11.dp.toPx()), strokeWidth = 2.5.dp.toPx())
                        drawLine(color, Offset(10.dp.toPx(), 17.dp.toPx()), Offset(size.width - 10.dp.toPx(), 17.dp.toPx()), strokeWidth = 2.5.dp.toPx())
                    }
                }
                IconButton(onClick = onToggleSortOrder, modifier = Modifier.semantics {
                    contentDescription = if (sortAscending) "Sort Z to A" else "Sort A to Z"
                }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (sortAscending) "A\nZ" else "Z\nA", color = Color.White, fontSize = 11.sp, lineHeight = 11.sp)
                        Text(if (sortAscending) "↓" else "↑", color = Color.White, fontSize = 27.sp)
                    }
                }
            }
        }
    }
}
