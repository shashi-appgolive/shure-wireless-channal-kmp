package com.shure.wireless.channels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shure.wireless.channels.devices.domain.model.AudioMeterChange
import com.shure.wireless.channels.devices.domain.model.DiscoveredDevice
import com.shure.wireless.channels.devices.domain.model.RfMeterChange
import com.shure.wireless.channels.devices.domain.model.RECEIVER_ID_MAX_LENGTH
import com.shure.wireless.channels.ui.components.DeviceConnectionInfo
import com.shure.wireless.channels.ui.components.DeviceReceiverInfo
import com.shure.wireless.channels.ui.components.ChannelAudioSettingsScreen
import com.shure.wireless.channels.ui.components.ChannelRfSettingsScreen
import com.shure.wireless.channels.ui.components.ReceiverSettingsScreen
import com.shure.wireless.channels.ui.theme.ShureColors

@Composable
internal fun ChannelDetailScreen(
    device: DiscoveredDevice,
    channelIndex: Int,
    audioMeters: Map<String, AudioMeterChange>,
    rfMeters: Map<String, RfMeterChange>,
    meterProgress: (Double, String) -> Float,
    onUpdateDeviceName: (String, String) -> Unit,
    onUpdateAudioGain: (String, Int) -> Unit,
    isUpdatingDeviceName: Boolean,
    isUpdatingAudioGain: Boolean,
    errorMessage: String?,
    onClearError: () -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
) {
    val model = device.interfaceInfo?.model ?: "Shure device"
    val rfChannel = device.features.rfChannels.getOrNull(channelIndex)
    val audioChannel = device.features.audioChannels.getOrNull(channelIndex)
    val channelName = audioChannel?.features?.name
        ?: rfChannel?.assignedRfProfile ?: rfChannel?.tuning?.channel ?: "Channel ${channelIndex + 1}"
    var showNameEditor by remember { mutableStateOf(false) }
    var nameInput by remember(device.id) { mutableStateOf(device.features.name ?: device.id) }
    var showReceiverSettings by remember(device.id, channelIndex) { mutableStateOf(false) }
    var showAudioSettings by remember(device.id, channelIndex) { mutableStateOf(false) }
    var showRfSettings by remember(device.id, channelIndex) { mutableStateOf(false) }

    if (showAudioSettings && audioChannel != null) {
        ChannelAudioSettingsScreen(
            channel = audioChannel,
            onBack = { showAudioSettings = false },
            onUpdateAudioGain = onUpdateAudioGain,
            isUpdatingAudioGain = isUpdatingAudioGain,
            errorMessage = errorMessage,
            onClearError = onClearError,
            surfaceColor = Color(0xFF1C1C1E),
            mutedColor = ShureColors.TextMuted,
        )
        return
    }

    if (showRfSettings && rfChannel != null) {
        ChannelRfSettingsScreen(
            channel = rfChannel,
            deviceRfBand = device.features.rfBand,
            onBack = { showRfSettings = false },
            surfaceColor = Color(0xFF1C1C1E),
            mutedColor = ShureColors.TextMuted,
        )
        return
    }

    if (showReceiverSettings) {
        ReceiverSettingsScreen(
            device = device,
            channelIndex = channelIndex,
            onBack = { showReceiverSettings = false },
            surfaceColor = Color(0xFF1C1C1E),
            mutedColor = ShureColors.TextMuted,
            isUpdatingReceiverId = isUpdatingDeviceName,
            errorMessage = errorMessage,
            onClearError = onClearError,
            onUpdateReceiverId = onUpdateDeviceName,
        )
        return
    }

    Box(Modifier.fillMaxSize().background(Color.Black).windowInsetsPadding(WindowInsets.safeDrawing)) {
        Column(Modifier.widthIn(max = 640.dp).fillMaxSize().align(Alignment.TopCenter)) {
            ChannelDetailToolbar(
                channelName = channelName,
                onBack = onBack,
                onEditDeviceName = { nameInput = device.features.name ?: device.id; showNameEditor = true },
                onRefresh = onRefresh,
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp),
            ) {
                errorMessage?.let { message -> item { ErrorBanner(message, onClearError) } }
                item { ChannelSectionTitle("CHANNEL RADIO FREQUENCY") }
                rfChannel?.let { channel -> item { RfChannelPanel(channel, device, rfMeters[channel.id]) { showRfSettings = true } } }
                item { ChannelSectionTitle("CHANNEL AUDIO") }
                audioChannel?.let { channel ->
                    item {
                        AudioChannelPanel(
                            channel = channel,
                            model = model,
                            meter = audioMeters[channel.id],
                            meterProgress = meterProgress,
                            isUpdatingAudioGain = isUpdatingAudioGain,
                            onUpdateAudioGain = onUpdateAudioGain,
                            onAudioSettingsClick = { showAudioSettings = true },
                        )
                    }
                }
                item { ChannelSectionTitle("DEVICE CONNECTION") }
                item { DeviceConnectionInfo(surfaceColor = Color(0xFF1C1C1E), mutedColor = ShureColors.TextMuted) }
                item { ChannelSectionTitle("RECEIVER") }
                item {
                    DeviceReceiverInfo(
                        receiverName = device.features.name ?: device.id,
                        receiverModel = model,
                        channelNumber = channelIndex + 1,
                        surfaceColor = Color(0xFF1C1C1E),
                        mutedColor = ShureColors.TextMuted,
                        onAllSettingsClick = { showReceiverSettings = true },
                    )
                }
            }
        }
    }
    if (showNameEditor) {
        AlertDialog(
            onDismissRequest = { showNameEditor = false },
            title = { Text("Update device name") },
            text = { OutlinedTextField(nameInput, { nameInput = it.take(RECEIVER_ID_MAX_LENGTH) }, label = { Text("Receiver ID (max $RECEIVER_ID_MAX_LENGTH characters)") }, singleLine = true) },
            confirmButton = {
                Button(onClick = { onUpdateDeviceName(device.id, nameInput); showNameEditor = false }, enabled = nameInput.isNotBlank() && nameInput.length <= RECEIVER_ID_MAX_LENGTH && !isUpdatingDeviceName) {
                    Text("Update")
                }
            },
            dismissButton = { OutlinedButton(onClick = { showNameEditor = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun ChannelSectionTitle(title: String) {
    Text(
        title,
        color = ShureColors.TextMuted,
        fontSize = 14.sp,
        modifier = Modifier.fillMaxWidth().background(Color.Black).padding(horizontal = 20.dp, vertical = 12.dp),
    )
}
