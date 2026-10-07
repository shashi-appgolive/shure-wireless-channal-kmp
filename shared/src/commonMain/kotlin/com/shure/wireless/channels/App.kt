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
        onConnect = viewModel::connect,
        onDiscover = viewModel::discoverDevices,
        onGetDeviceModels = viewModel::getDeviceModels,
        onToggleEvents = viewModel::toggleEventListening,
        onRefreshDatabase = viewModel::refreshDevices,
        onClearLogs = viewModel::clearLogs,
        onClearError = viewModel::clearError,
    )
}

@Composable
fun DeviceConsoleScreen(
    state: DeviceUiState,
    onConnect: (String) -> Unit = {},
    onDiscover: (String) -> Unit = {},
    onGetDeviceModels: (String) -> Unit = {},
    onToggleEvents: () -> Unit = {},
    onRefreshDatabase: () -> Unit = {},
    onClearLogs: () -> Unit = {},
    onClearError: () -> Unit = {},
) {
    var address by remember { mutableStateOf(defaultGraphQlBaseUrl()) }
    var selectedDevice by remember { mutableStateOf<StoredDevice?>(null) }
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
                        onConnect = { onConnect(address) },
                        onDiscover = { onDiscover(address) },
                        onGetDeviceModels = { onGetDeviceModels(address) },
                        onToggleEvents = onToggleEvents,
                        onRefreshDatabase = onRefreshDatabase,
                    )
                }
                state.errorMessage?.let { message -> item { ErrorBanner(message, onClearError) } }
                item { StatusStrip(state) }
                item { SectionHeader("DEVICE MODELS", "${state.deviceModels.size} available") }
                if (state.deviceModels.isEmpty()) {
                    item { EmptyPanel("Use Get Models to load supported device models.") }
                } else {
                    item { Text(state.deviceModels.joinToString(", "), color = MutedText) }
                }
                item { SectionHeader("PERSISTED DEVICES", "Room 3 · live query") }
                if (state.devices.isEmpty()) {
                    item { EmptyPanel("No devices in Room. Connect or run discovery.") }
                } else {
                    items(state.devices, key = { it.id }) { device ->
                        DeviceRow(
                            device = device,
                            isConnected = state.connectedDevice?.id == device.id,
                            onClick = { selectedDevice = device },
                        )
                    }
                }
                selectedDevice?.let { device ->
                    item {
                        DeviceDetailsPanel(
                            device = device,
                            onDismiss = { selectedDevice = null },
                        )
                    }
                }
                item { SectionHeader("DEVICE EVENTS", if (state.isListening) "stream active" else "stream stopped") }
                if (state.events.isEmpty()) {
                    item { EmptyPanel("Start Listen Events to receive simulated device changes.") }
                } else {
                    items(state.events.takeLast(6).reversed()) { event ->
                        DeviceEventRow(event)
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SectionHeader("OPERATION LOG", "${state.logs.size} entries")
                        OutlinedButton(onClick = onClearLogs) { Text("Clear") }
                    }
                }
                if (state.logs.isEmpty()) {
                    item { EmptyPanel("Operation logs will appear here.") }
                } else {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            state.logs.takeLast(60).forEach { entry -> LogRow(entry) }
                        }
                    }
                }
                item { Spacer(Modifier.height(20.dp)) }
            }
        }
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
            Text("KMP API and persistence demonstration", color = MutedText)
        }
        StatusPill(state.connectedDevice?.let { "CONNECTED" } ?: "READY", state.connectedDevice != null)
    }
}

@Composable
private fun ActionPanel(
    address: String,
    onAddressChange: (String) -> Unit,
    state: DeviceUiState,
    onConnect: () -> Unit,
    onDiscover: () -> Unit,
    onGetDeviceModels: () -> Unit,
    onToggleEvents: () -> Unit,
    onRefreshDatabase: () -> Unit,
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
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OperationButton(
                text = if (state.isConnecting) "Connecting…" else "Connect",
                loading = state.isConnecting,
                enabled = !state.isConnecting,
                onClick = onConnect,
                modifier = Modifier.weight(1f),
            )
            OperationButton(
                text = if (state.isDiscovering) "Discovering…" else "Discover",
                loading = state.isDiscovering,
                enabled = !state.isDiscovering,
                onClick = onDiscover,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(10.dp))
        OperationButton(
            text = if (state.isLoadingDeviceModels) "Loading Models…" else "Get Device Models",
            loading = state.isLoadingDeviceModels,
            enabled = !state.isLoadingDeviceModels,
            onClick = onGetDeviceModels,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onToggleEvents, modifier = Modifier.weight(1f)) {
                Text(if (state.isListening) "Stop Events" else "Listen Events")
            }
            OutlinedButton(onClick = onRefreshDatabase, modifier = Modifier.weight(1f)) {
                Text(if (state.isLoading) "Reading Room…" else "Read Database")
            }
        }
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
