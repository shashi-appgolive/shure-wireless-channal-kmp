package com.shure.wireless.channels

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
import org.koin.compose.viewmodel.koinViewModel

private val ConsoleBackground = ShureColors.Black
private val ConsoleSurface = ShureColors.Surface
private val ConsoleBorder = ShureColors.Border
private val ShureGreen = ShureColors.Green
private val MutedText = ShureColors.TextMuted
private val ErrorRed = ShureColors.Error
private val EventBlue = ShureColors.Event

@Composable
fun App(viewModel: DeviceViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DeviceConsoleScreen(
        state = uiState,
        onDiscover = viewModel::discoverDevices,
        onGetDeviceModels = viewModel::getDeviceModels,
        onStartListening = viewModel::startListening,
        onStopListening = viewModel::stopListening,
        onStartRfListening = viewModel::startRfListening,
        onStopRfListening = viewModel::stopRfListening,
        onUpdateDeviceName = { deviceId, name -> viewModel.updateDeviceName(defaultGraphQlBaseUrl(), deviceId, name) },
        onClearError = viewModel::clearError,
    )
}

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
                        onGetDeviceModels = { onGetDeviceModels(address) },
                    )
                }
                state.errorMessage?.let { message -> item { ErrorBanner(message, onClearError) } }
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = state.deviceModels.isNotEmpty()) { showDeviceModels = true },
                        colors = CardDefaults.cardColors(containerColor = ConsoleSurface),
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text("DEVICE MODELS", color = ShureGreen, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Text(
                                if (state.deviceModels.isEmpty()) "Use Get Device Models to load supported models."
                                else "${state.deviceModels.size} models available · Tap to view",
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
private fun DeviceModelsScreen(
    models: List<String>,
    onBack: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("DEVICE MODELS", color = ShureGreen, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            OutlinedButton(onClick = onBack) { Text("Back") }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(models, key = { it }) { model ->
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = ConsoleSurface)) {
                    Text(model, Modifier.padding(14.dp), color = Color.White, fontWeight = FontWeight.Bold)
                }
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
    onDismiss: () -> Unit,
    fullScreen: Boolean = false,
) {
    var isEditingName by remember { mutableStateOf(false) }
    var editedName by remember(device.id) { mutableStateOf(device.features.name ?: device.interfaceInfo?.model.orEmpty()) }
    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("DEVICE DETAILS", color = ShureGreen, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!isEditingName) {
                    OutlinedButton(onClick = { isEditingName = true }) { Text("Edit") }
                }
                OutlinedButton(onClick = onDismiss) { Text(if (fullScreen) "Back" else "Close") }
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
                OutlinedButton(onClick = { if (isListening) onStopListening(channel.id) else onStartListening(channel.id) }) {
                    Text(if (isListening) "Stop" else "Listen")
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
                OutlinedButton(onClick = { if (isListening) onStopRfListening(channel.id) else onStartRfListening(channel.id) }) {
                    Text(if (isListening) "Stop" else "Listen")
                }
            }
        }
    }
    if (fullScreen) {
        Column(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical))
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) { content() }
    } else {
        ConsoleCard { content() }
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
    onGetDeviceModels: () -> Unit,
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
        OperationButton(
            text = if (state.isLoadingDeviceModels) "Loading Models…" else "Get Device Models",
            loading = state.isLoadingDeviceModels,
            enabled = !state.isLoadingDeviceModels,
            onClick = onGetDeviceModels,
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
private fun ConsoleCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, ConsoleBorder, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ConsoleSurface),
    ) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun SectionHeader(title: String, detail: String) {
    Column {
        Text(title, color = ShureGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp)
        Text(detail, color = MutedText, fontSize = 11.sp)
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

@Composable
private fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(ErrorRed.copy(alpha = 0.12f), RoundedCornerShape(10.dp)).padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(message, color = ErrorRed, modifier = Modifier.weight(1f))
        OutlinedButton(onClick = onDismiss) { Text("Dismiss") }
    }
}

@Preview
@Composable
private fun DeviceConsolePreview() {
    DeviceConsoleScreen(
        state = DeviceUiState(
            devices = listOf(
                StoredDevice(
                    id = "demo",
                    name = "Stage Receiver A",
                    model = "ULXD4Q",
                    ipAddress = "192.168.1.20",
                    firmwareVersion = "2.8.1",
                    lastSeenAtEpochMillis = 1L,
                ),
            ),
            events = listOf(DeviceEvent("demo", DeviceEventType.SIGNAL_CHANGED, "RF level changed to -48 dBm")),
            logs = listOf(
                LogEntry(1, LogLevel.DEBUG, "DeviceViewModel", "Connect requested for 192.168.1.20"),
                LogEntry(2, LogLevel.INFO, "RoomDeviceRepository", "Room emitted 1 persisted devices"),
            ),
        ),
    )
}
