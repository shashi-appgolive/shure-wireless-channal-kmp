package com.shure.wireless.channels.ui.diagnostics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shure.wireless.channels.core.common.LogEntry
import com.shure.wireless.channels.core.common.LogLevel
import com.shure.wireless.channels.di.defaultGraphQlBaseUrl
import com.shure.wireless.channels.devices.domain.model.DeviceEvent
import com.shure.wireless.channels.devices.domain.model.DeviceEventType
import com.shure.wireless.channels.devices.domain.model.StoredDevice
import com.shure.wireless.channels.devices.domain.model.DiscoveredDevice
import com.shure.wireless.channels.devices.presentation.DeviceUiState
import com.shure.wireless.channels.devices.presentation.DeviceViewModel
import com.shure.wireless.channels.ui.theme.ShureColors
import com.shure.wireless.channels.ui.theme.ShureTheme
import com.shure.wireless.channels.ui.components.AudioMeter
import com.shure.wireless.channels.ui.components.TinyRfMeter
import com.shure.wireless.channels.ui.components.formatWorkbenchFrequency
import com.shure.wireless.channels.ui.components.ErrorBanner
import com.shure.wireless.channels.ui.workbench.WorkbenchBackground
import com.shure.wireless.channels.ui.workbench.WorkbenchSurface
import com.shure.wireless.channels.ui.workbench.WorkbenchGreen
import com.shure.wireless.channels.ui.workbench.WorkbenchMutedText
import com.shure.wireless.channels.ui.workbench.WorkbenchError
import com.shure.wireless.channels.di.defaultGraphQlBaseUrl

private val ConsoleBackground = WorkbenchBackground
private val ConsoleSurface = WorkbenchSurface
private val ConsoleBorder = ShureColors.Border
private val ShureGreen = WorkbenchGreen
private val MutedText = WorkbenchMutedText
private val ErrorRed = WorkbenchError
private val EventBlue = ShureColors.Event
@Composable
fun DeviceConsoleScreen(
    state: DeviceUiState,
    onDiscover: (String) -> Unit = {},
    onGetDeviceModels: (String) -> Unit = {},
    onStartListening: (String) -> Unit = {},
    onStopListening: (String) -> Unit = {},
    onStartRfListening: (String) -> Unit = {},
    onStopRfListening: (String) -> Unit = {},
    onUpdateDeviceName: (String, String) -> Unit = { _, _ -> },
    onUpdateAudioGain: (String, Int) -> Unit = { _, _ -> },
    onClearError: () -> Unit = {},
) {
    var address by remember { mutableStateOf(defaultGraphQlBaseUrl()) }
    var selectedConnection by remember { mutableStateOf<DiscoveredDevice?>(null) }
    var showDeviceModels by remember { mutableStateOf(false) }
    if (showDeviceModels) {
        ShureTheme {
            Surface(modifier = Modifier.fillMaxSize(), color = ConsoleBackground) {
                DeviceModelsScreen(
                    models = state.deviceModels,
                    isLoading = state.isLoadingDeviceModels,
                    onBack = { showDeviceModels = false },
                )
            }
        }
        return
    }
    if (selectedConnection != null) {
        ShureTheme {
            Surface(modifier = Modifier.fillMaxSize(), color = ConsoleBackground) {
                ConnectionDetailsPanel(
                    device = requireNotNull(selectedConnection),
                    state = state,
                    onStartListening = onStartListening,
                    onStopListening = onStopListening,
                    onStartRfListening = onStartRfListening,
                    onStopRfListening = onStopRfListening,
                    onUpdateDeviceName = onUpdateDeviceName,
                    onUpdateAudioGain = onUpdateAudioGain,
                    onDismiss = { selectedConnection = null },
                    fullScreen = true,
                )
            }
        }
        return
    }
    ShureTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = ConsoleBackground) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical)),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item { ConsoleHeader(state) }
                item {
                    ActionPanel(
                        address = address,
                        onAddressChange = { address = it },
                        state = state,
                        onDiscover = { onDiscover(address) },
                    )
                }
                state.errorMessage?.let { message -> item { ErrorBanner(message, onClearError) } }
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onGetDeviceModels(address); showDeviceModels = true },
                        colors = CardDefaults.cardColors(containerColor = ConsoleSurface),
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text("DEVICE MODELS", color = ShureGreen, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Text(
                                if (state.deviceModels.isEmpty()) "View supported device models"
                                else "${state.deviceModels.size} models available · Tap to refresh",
                                color = MutedText,
                            )
                        }
                    }
                }
                item { SectionHeader("DISCOVERED CONNECTIONS", "${state.discoveredConnections.size} devices") }
                if (state.discoveredConnections.isEmpty()) {
                    item { EmptyPanel("Use Discover Connections to load detailed device capabilities.") }
                } else {
                    itemsIndexed(state.discoveredConnections, key = { index, device -> "${device.id}-$index" }) { _, device ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedConnection = device },
                            colors = CardDefaults.cardColors(containerColor = ConsoleSurface),
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(
                                    device.features.name ?: device.interfaceInfo?.model ?: device.id,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    "${device.status ?: "Unknown"} · ${device.features.audioChannels.size} audio · ${device.features.rfChannels.size} RF",
                                    color = MutedText,
                                )
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(20.dp)) }
            }
        }
    }
}

@Composable
private fun ConnectionDetailsPanel(
    device: DiscoveredDevice,
    state: DeviceUiState,
    onStartListening: (String) -> Unit,
    onStopListening: (String) -> Unit,
    onStartRfListening: (String) -> Unit,
    onStopRfListening: (String) -> Unit,
    onUpdateDeviceName: (String, String) -> Unit,
    onUpdateAudioGain: (String, Int) -> Unit,
    onDismiss: () -> Unit,
    fullScreen: Boolean = false,
) {
    var isEditingName by remember { mutableStateOf(false) }
    var editedName by remember(device.id) { mutableStateOf(device.features.name ?: device.interfaceInfo?.model.orEmpty()) }
    var gainChannelId by remember { mutableStateOf<String?>(null) }
    var gainInput by remember { mutableStateOf("") }
    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("DEVICE DETAILS", color = ShureGreen, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!isEditingName) {
                    IconButton(onClick = { isEditingName = true }) { Text("✎", color = ShureGreen, fontSize = 20.sp) }
                }
                IconButton(onClick = onDismiss) { Text("×", color = Color.White, fontSize = 26.sp) }
            }
        }
        Spacer(Modifier.height(8.dp))
        if (isEditingName) {
            OutlinedTextField(
                value = editedName,
                onValueChange = { editedName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Device name") },
                singleLine = true,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onUpdateDeviceName(device.id, editedName); isEditingName = false }) { Text("Save") }
                OutlinedButton(onClick = { isEditingName = false }) { Text("Cancel") }
            }
        } else {
            Text(device.features.name ?: device.interfaceInfo?.model ?: device.id, color = Color.White, fontWeight = FontWeight.Bold)
        }
        Text("${device.interfaceInfo?.category ?: "Unknown"} · ${device.status ?: "Unknown"}", color = MutedText)
        Spacer(Modifier.height(12.dp))
        Text("AUDIO CHANNELS", color = ShureGreen, fontWeight = FontWeight.Bold)
        device.features.audioChannels.forEachIndexed { index, channel ->
            val meter = state.audioMeters[channel.id]
            val isListening = channel.id in state.listeningChannelIds
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("${index + 1}. ${channel.features.name ?: channel.id}", color = Color.White)
                    Text("Gain: ${channel.features.gain ?: "—"} · Peak: ${meter?.peakLevel ?: "—"} · RMS: ${meter?.rmsLevel ?: "—"}", color = MutedText)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(onClick = { gainChannelId = channel.id; gainInput = channel.features.gain?.toInt()?.toString().orEmpty() }) {
                        Text("✎", color = ShureGreen, fontSize = 20.sp)
                    }
                    IconButton(onClick = { if (isListening) onStopListening(channel.id) else onStartListening(channel.id) }) {
                        Text(if (isListening) "■" else "▶", color = ShureGreen, fontSize = 16.sp)
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("RF CHANNELS", color = ShureGreen, fontWeight = FontWeight.Bold)
        device.features.rfChannels.forEachIndexed { index, channel ->
            val meter = state.rfMeters[channel.id]
            val isListening = channel.id in state.listeningRfChannelIds
            val antennaA = meter?.antennas?.firstOrNull { it.antenna == "ANTENNA_A" }?.level
            val antennaB = meter?.antennas?.firstOrNull { it.antenna == "ANTENNA_B" }?.level
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("${device.features.serialNumber ?: "RF Channel ${index + 1}"}", color = Color.White)
                    Text("A_A: ${antennaA ?: "—"} · A_B: ${antennaB ?: "—"}", color = MutedText)
                }
                IconButton(onClick = { if (isListening) onStopRfListening(channel.id) else onStartRfListening(channel.id) }) {
                    Text(if (isListening) "■" else "▶", color = ShureGreen, fontSize = 16.sp)
                }
            }
        }
    }
    if (fullScreen) {
        Column(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) { content() }
    } else {
        ConsoleCard { content() }
    }
    gainChannelId?.let { channelId ->
        AlertDialog(
            onDismissRequest = { if (!state.isUpdatingAudioGain) gainChannelId = null },
            title = { Text("Update gain") },
            text = {
                OutlinedTextField(
                    value = gainInput,
                    onValueChange = { value -> if (value.isEmpty() || value.toIntOrNull() != null) gainInput = value },
                    label = { Text("Gain") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
            },
            confirmButton = {
                Button(
                    enabled = !state.isUpdatingAudioGain && gainInput.toIntOrNull() != null,
                    onClick = { onUpdateAudioGain(channelId, gainInput.toInt()); gainChannelId = null },
                ) { Text(if (state.isUpdatingAudioGain) "Saving…" else "Save") }
            },
            dismissButton = { OutlinedButton(enabled = !state.isUpdatingAudioGain, onClick = { gainChannelId = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun ConsoleHeader(state: DeviceUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text("SHURE", color = ShureGreen, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
            Text("Wireless Operations Console", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Razor SDK reference application", color = MutedText)
        }
        StatusPill(state.connectedDevice?.let { "CONNECTED" } ?: "READY", state.connectedDevice != null)
    }
}

@Composable
private fun ActionPanel(
    address: String,
    onAddressChange: (String) -> Unit,
    state: DeviceUiState,
    onDiscover: () -> Unit,
) {
    ConsoleCard {
        Text("OPERATIONS", color = ShureGreen, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = address,
            onValueChange = onAddressChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Device IP address") },
            singleLine = true,
        )
        Spacer(Modifier.height(12.dp))
        OperationButton(
            text = if (state.isDiscovering) "Discovering…" else "Discover Devices",
            loading = state.isDiscovering,
            enabled = !state.isDiscovering,
            onClick = onDiscover,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun OperationButton(
    text: String,
    loading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = ShureGreen, contentColor = Color.Black),
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(15.dp), strokeWidth = 2.dp, color = Color.Black)
            Spacer(Modifier.size(8.dp))
        }
        Text(text, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StatusStrip(state: DeviceUiState) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MetricCard("ROOM", state.devices.size.toString(), "devices", Modifier.weight(1f))
        MetricCard("EVENTS", state.events.size.toString(), if (state.isListening) "listening" else "paused", Modifier.weight(1f))
        MetricCard("WRITE", if (state.isSaving) "BUSY" else "IDLE", "database", Modifier.weight(1f))
    }
}

@Composable
private fun MetricCard(label: String, value: String, detail: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = ConsoleSurface), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text(label, color = MutedText, fontSize = 10.sp, letterSpacing = 1.sp)
            Text(value, color = ShureGreen, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(detail, color = MutedText, fontSize = 11.sp)
        }
    }
}

@Composable
private fun DeviceRow(
    device: StoredDevice,
    isConnected: Boolean,
    onClick: () -> Unit,
) {
    ConsoleCard {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Text(device.name, fontWeight = FontWeight.Bold)
                Text("${device.model ?: "Unknown model"} · ${device.ipAddress ?: "No IP"}", color = MutedText)
                Text(device.deviceDetailText(), color = MutedText, fontSize = 12.sp)
            }
            StatusPill(if (isConnected) "LIVE" else "SAVED", isConnected)
        }
    }
}

@Composable
private fun DeviceDetailsPanel(
    device: StoredDevice,
    onDismiss: () -> Unit,
) {
    ConsoleCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionHeader("DEVICE DETAILS", device.name)
            OutlinedButton(onClick = onDismiss) { Text("Close") }
        }
        Spacer(Modifier.height(10.dp))
        DeviceDetailLine("ID", device.id)
        DeviceDetailLine("Hardware ID", device.hardwareId ?: "-")
        DeviceDetailLine("Status", device.status ?: "-")
        DeviceDetailLine("Model", device.model ?: "-")
        DeviceDetailLine("Category", device.category ?: "-")
        DeviceDetailLine("IP Address", device.ipAddress ?: "-")
        DeviceDetailLine("Firmware", device.firmwareVersion ?: "-")
        DeviceDetailLine("Last Seen", device.lastSeenAtEpochMillis.toString())
    }
}

@Composable
private fun DeviceDetailLine(label: String, value: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, color = MutedText, fontSize = 10.sp, letterSpacing = 1.sp)
        Text(value, color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
    }
}

private fun StoredDevice.deviceDetailText(): String {
    val apiDetails = listOfNotNull(status, category, hardwareId).takeIf { it.isNotEmpty() }
    if (apiDetails != null) return apiDetails.joinToString(" · ")

    return "Firmware ${firmwareVersion ?: "-"} · persisted in Room"
}

@Composable
private fun DeviceEventRow(event: DeviceEvent) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(EventBlue.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
            .border(1.dp, EventBlue.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(8.dp).background(EventBlue, CircleShape))
        Spacer(Modifier.size(10.dp))
        Column {
            Text(event.type.name.replace('_', ' '), color = EventBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(event.message)
            Text(event.deviceId, color = MutedText, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun LogRow(entry: LogEntry) {
    val levelColor = when (entry.level) {
        LogLevel.DEBUG -> MutedText
        LogLevel.INFO -> ShureGreen
        LogLevel.WARN -> Color(0xFFFFC857)
        LogLevel.ERROR -> ErrorRed
    }
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.45f))
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(entry.sequence.toString().padStart(3, '0'), color = Color(0xFF536059), fontFamily = FontFamily.Monospace, fontSize = 10.sp)
            Spacer(Modifier.size(6.dp))
            Text(entry.level.name.first().toString(), color = levelColor, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Spacer(Modifier.size(6.dp))
            Text(entry.tag, color = EventBlue, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
        }
        Text(
            entry.message,
            color = Color(0xFFD4DDD8),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            lineHeight = 15.sp,
            modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
        )
    }
}

@Composable
private fun StatusPill(label: String, active: Boolean) {
    Row(
        modifier = Modifier.background(if (active) ShureGreen.copy(alpha = 0.14f) else ConsoleSurface, RoundedCornerShape(50))
            .border(1.dp, if (active) ShureGreen.copy(alpha = 0.5f) else ConsoleBorder, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(7.dp).background(if (active) ShureGreen else MutedText, CircleShape))
        Spacer(Modifier.size(7.dp))
        Text(label, color = if (active) ShureGreen else MutedText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EmptyPanel(message: String) {
    Text(
        message,
        modifier = Modifier.fillMaxWidth().border(1.dp, ConsoleBorder, RoundedCornerShape(10.dp)).padding(16.dp),
        color = MutedText,
    )
}
