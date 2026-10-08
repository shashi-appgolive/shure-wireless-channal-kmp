package com.shure.wireless.channels.ui.workbench

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.shure.wireless.channels.devices.domain.model.DiscoveredDevice
import com.shure.wireless.channels.devices.presentation.DeviceUiState
import com.shure.wireless.channels.ui.components.ErrorBanner
import com.shure.wireless.channels.ui.theme.ShureTheme
import com.shure.wireless.channels.di.defaultGraphQlBaseUrl

@Composable
fun WorkbenchScreen(state: DeviceUiState, actions: WorkbenchActions) {
    var address by remember { mutableStateOf(defaultGraphQlBaseUrl()) }
    var draftAddress by remember { mutableStateOf(address) }
    var showEndpointEditor by remember { mutableStateOf(false) }
    var selectedChannel by remember { mutableStateOf<Pair<DiscoveredDevice, Int>?>(null) }

    LaunchedEffect(address) { actions.discover(address) }

    if (selectedChannel != null) {
        val (device, channelIndex) = requireNotNull(selectedChannel)
        ShureTheme {
            DeviceDetailsScreen(
                device = device,
                channelIndex = channelIndex,
                audioMeters = state.audioMeters,
                rfMeters = state.rfMeters,
                meterProgress = actions.meterProgress,
                actions = DeviceDetailsActions(
                    startAudioListening = actions.startAudioListening,
                    stopAudioListening = actions.stopAudioListening,
                    startRfListening = actions.startRfListening,
                    stopRfListening = actions.stopRfListening,
                    updateDeviceName = actions.updateDeviceName,
                    updateAudioGain = actions.updateAudioGain,
                    refresh = { actions.discover(address) },
                    back = { selectedChannel = null },
                ),
                surfaceColor = WorkbenchSurface,
                accentColor = WorkbenchGreen,
                mutedColor = WorkbenchMutedText,
            )
        }
        return
    }

    DisposableEffect(state.discoveredConnections) {
        val audioIds = state.discoveredConnections.flatMap { it.features.audioChannels.map { channel -> channel.id } }
        val rfIds = state.discoveredConnections.flatMap { it.features.rfChannels.map { channel -> channel.id } }
        audioIds.forEach(actions.startAudioListening)
        rfIds.forEach(actions.startRfListening)
        onDispose {
            audioIds.forEach(actions.stopAudioListening)
            rfIds.forEach(actions.stopRfListening)
        }
    }

    ShureTheme {
        Surface(Modifier.fillMaxSize(), color = WorkbenchBackground) {
            Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
                Row(Modifier.fillMaxWidth().background(WorkbenchSurface).padding(horizontal = 24.dp, vertical = 24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Inventory", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        IconButton(onClick = { draftAddress = address; showEndpointEditor = true }) { Text("✎", color = WorkbenchGreen, fontSize = 24.sp) }
                        IconButton(onClick = { actions.discover(address) }) { Text("↻", color = WorkbenchGreen, fontSize = 24.sp) }
                    }
                }
                if (state.discoveredConnections.isNotEmpty()) {
                    WorkbenchDeviceList(state.discoveredConnections, state.audioMeters, state.rfMeters, actions.meterProgress, WorkbenchMutedText, WorkbenchGreen) { device, index -> selectedChannel = device to index }
                } else {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 28.dp)) {
                        Text("Device Discovery", color = WorkbenchGreen, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(42.dp))
                        Text(if (state.isDiscovering) "Discovering\ndevices on\nyour network" else "Devices on\nyour network", color = Color.White, fontSize = 38.sp, lineHeight = 46.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(48.dp))
                        if (state.isDiscovering) CircularProgressIndicator(Modifier.size(18.dp), color = WorkbenchGreen, strokeWidth = 3.dp)
                        state.errorMessage?.let { ErrorBanner(it, actions.clearError, onRetry = { actions.discover(address) }) }
                    }
                }
            }
        }
        if (showEndpointEditor) {
            AlertDialog(onDismissRequest = { showEndpointEditor = false }, title = { Text("Base URL") }, text = { OutlinedTextField(draftAddress, { draftAddress = it }, label = { Text("GraphQL base URL") }, singleLine = true) }, confirmButton = { Button(onClick = { address = draftAddress.trim(); showEndpointEditor = false }, enabled = draftAddress.isNotBlank()) { Text("Update") } }, dismissButton = { OutlinedButton(onClick = { showEndpointEditor = false }) { Text("Cancel") } })
        }
    }
}
