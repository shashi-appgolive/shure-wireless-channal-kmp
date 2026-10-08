package com.shure.wireless.channels

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shure.wireless.channels.devices.presentation.DeviceUiState
import com.shure.wireless.channels.ui.theme.ShureColors
import com.shure.wireless.channels.ui.theme.ShureTheme

@Composable
internal fun InventoryScreen(
    state: DeviceUiState,
    onDiscover: () -> Unit,
    onEndpointChange: (String) -> Unit,
    onSearchChange: (String) -> Unit,
    onToggleOnlineFilter: () -> Unit,
    onToggleSortOrder: () -> Unit,
    onSelectChannel: (String, Int) -> Unit,
    onCloseChannel: () -> Unit,
    onUpdateDeviceName: (String, String) -> Unit,
    onUpdateAudioGain: (String, Int) -> Unit,
    meterProgress: (Double, String) -> Float,
    onClearError: () -> Unit,
) {
    val selectedDevice = state.discoveredConnections.firstOrNull { it.id == state.inventory.selectedDeviceId }
    if (selectedDevice != null) {
        ShureTheme {
            ChannelDetailScreen(
                device = selectedDevice,
                channelIndex = state.inventory.selectedChannelIndex,
                audioMeters = state.audioMeters,
                rfMeters = state.rfMeters,
                meterProgress = meterProgress,
                onUpdateDeviceName = onUpdateDeviceName,
                onUpdateAudioGain = onUpdateAudioGain,
                isUpdatingAudioGain = state.isUpdatingAudioGain,
                errorMessage = state.errorMessage,
                onClearError = onClearError,
                onRefresh = onDiscover,
                onBack = onCloseChannel,
            )
        }
        return
    }

    var draftAddress by remember { mutableStateOf("") }
    var showEndpointEditor by remember { mutableStateOf(false) }
    ShureTheme {
        Surface(Modifier.fillMaxSize(), color = Color.Black) {
            Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
                InventoryToolbar(
                    onlineCount = state.inventory.onlineCount,
                    totalDevices = state.inventory.totalDevices,
                    hasKnownStatuses = state.inventory.hasKnownStatuses,
                    onlineOnly = state.inventory.onlineOnly,
                    sortAscending = state.inventory.sortAscending,
                    onToggleOnlineFilter = onToggleOnlineFilter,
                    onToggleSortOrder = onToggleSortOrder,
                    onEditEndpoint = { draftAddress = state.inventory.endpoint; showEndpointEditor = true },
                    onRefresh = onDiscover,
                )
                InventorySearchBar(query = state.inventory.searchQuery, onQueryChange = onSearchChange)
                state.errorMessage?.let { message ->
                    ErrorBanner(message = message, onDismiss = onClearError, onRetry = onDiscover)
                }
                if (state.discoveredConnections.isNotEmpty()) {
                    InventoryDeviceList(
                        devices = state.inventory.visibleDevices,
                        audioMeters = state.audioMeters,
                        rfMeters = state.rfMeters,
                        meterProgress = meterProgress,
                        onDeviceClick = { device, index -> onSelectChannel(device.id, index) },
                    )
                } else {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 28.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⌁", color = ShureColors.Green, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.size(10.dp))
                            Text("Device Discovery", color = ShureColors.Green, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(42.dp))
                        Text(
                            if (state.isDiscovering) "Discovering\ndevices on\nyour network" else "Devices on\nyour network",
                            color = Color.White,
                            fontSize = 38.sp,
                            lineHeight = 46.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(48.dp))
                        if (state.isDiscovering) {
                            CircularProgressIndicator(Modifier.size(18.dp), color = ShureColors.Green, strokeWidth = 3.dp)
                        }
                    }
                }
            }
        }
        if (showEndpointEditor) {
            AlertDialog(
                onDismissRequest = { showEndpointEditor = false },
                title = { Text("Base URL") },
                text = { OutlinedTextField(draftAddress, { draftAddress = it }, label = { Text("GraphQL base URL") }, singleLine = true) },
                confirmButton = {
                    Button(onClick = { onEndpointChange(draftAddress); showEndpointEditor = false }, enabled = draftAddress.isNotBlank()) {
                        Text("Update")
                    }
                },
                dismissButton = { OutlinedButton(onClick = { showEndpointEditor = false }) { Text("Cancel") } },
            )
        }
    }
}
