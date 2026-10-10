package com.shure.wireless.channels.ui.workbench

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shure.wireless.channels.devices.domain.model.DiscoveredDevice
import com.shure.wireless.channels.devices.domain.model.AudioMeterChange
import com.shure.wireless.channels.devices.domain.model.RfMeterChange
import com.shure.wireless.channels.devices.domain.model.RECEIVER_ID_MAX_LENGTH
import com.shure.wireless.channels.ui.components.AudioChannelDetails
import com.shure.wireless.channels.ui.components.ChannelAudioSettingsScreen
import com.shure.wireless.channels.ui.components.ChannelRfSettingsScreen
import com.shure.wireless.channels.ui.components.DeviceConnectionInfo
import com.shure.wireless.channels.ui.components.DeviceDetailsHeader
import com.shure.wireless.channels.ui.components.DeviceReceiverInfo
import com.shure.wireless.channels.ui.components.ReceiverSettingsScreen
import com.shure.wireless.channels.ui.components.RfChannelDetails
import com.shure.wireless.channels.ui.components.settingsDeviceNoun

@Composable
fun DeviceDetailsScreen(
    device: DiscoveredDevice,
    channelIndex: Int,
    audioMeters: Map<String, AudioMeterChange>,
    rfMeters: Map<String, RfMeterChange>,
    meterProgress: (Double, String) -> Float,
    actions: DeviceDetailsActions,
    isUpdatingDeviceName: Boolean,
    isUpdatingAudioGain: Boolean,
    errorMessage: String?,
    onClearError: () -> Unit,
    surfaceColor: Color,
    accentColor: Color,
    mutedColor: Color,
) {
    val details = device.toDeviceDetailsUiModel(channelIndex)
    val deviceNoun = device.settingsDeviceNoun()
    val rfChannel = device.features.rfChannels.getOrNull(channelIndex)
    val audioChannel = device.features.audioChannels.getOrNull(channelIndex)
    var showNameEditor by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf(details.deviceName) }
    var displayDeviceName by remember { mutableStateOf(details.deviceName) }
    var gainInput by remember { mutableStateOf((audioChannel?.features?.gain ?: 0.0).toInt()) }
    var showReceiverSettings by remember(device.id, channelIndex) { mutableStateOf(false) }
    var showAudioSettings by remember(device.id, channelIndex) { mutableStateOf(false) }
    var showRfSettings by remember(device.id, channelIndex) { mutableStateOf(false) }

    LaunchedEffect(device.id, details.deviceName) {
        displayDeviceName = details.deviceName
        nameInput = details.deviceName
    }
    LaunchedEffect(audioChannel?.id, audioChannel?.features?.gain) {
        gainInput = (audioChannel?.features?.gain ?: 0.0).toInt()
    }

    DisposableEffect(audioChannel?.id) {
        audioChannel?.id?.let(actions.startAudioListening)
        onDispose { audioChannel?.id?.let(actions.stopAudioListening) }
    }
    DisposableEffect(rfChannel?.id) {
        rfChannel?.id?.let(actions.startRfListening)
        onDispose { rfChannel?.id?.let(actions.stopRfListening) }
    }

    if (showAudioSettings && audioChannel != null) {
        ChannelAudioSettingsScreen(
            channel = audioChannel,
            onBack = { showAudioSettings = false },
            onUpdateAudioGain = actions.updateAudioGain,
            isUpdatingAudioGain = isUpdatingAudioGain,
            errorMessage = errorMessage,
            onClearError = onClearError,
            surfaceColor = surfaceColor,
            mutedColor = mutedColor,
            accentColor = accentColor,
        )
        return
    }

    if (showRfSettings && rfChannel != null) {
        ChannelRfSettingsScreen(
            channel = rfChannel,
            deviceRfBand = device.features.rfBand,
            onBack = { showRfSettings = false },
            surfaceColor = surfaceColor,
            mutedColor = mutedColor,
            accentColor = accentColor,
        )
        return
    }

    if (showReceiverSettings) {
        ReceiverSettingsScreen(
            device = device,
            channelIndex = channelIndex,
            onBack = { showReceiverSettings = false },
            surfaceColor = surfaceColor,
            mutedColor = mutedColor,
            isUpdatingReceiverId = isUpdatingDeviceName,
            errorMessage = errorMessage,
            onClearError = onClearError,
            onUpdateReceiverId = actions.updateDeviceName,
        )
        return
    }

    Column(Modifier.fillMaxSize().background(surfaceColor).windowInsetsPadding(WindowInsets.safeDrawing)) {
        DeviceDetailsHeader(details.channelName, surfaceColor, accentColor, actions.back, { nameInput = displayDeviceName; showNameEditor = true }, actions.refresh)
        LazyColumn(contentPadding = PaddingValues(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { Text(displayDeviceName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 24.dp)) }
            item { Text("ALERTS (0)", color = mutedColor, modifier = Modifier.padding(horizontal = 24.dp)) }
            item { Text("CHANNEL RADIO FREQUENCY", color = mutedColor, modifier = Modifier.padding(horizontal = 24.dp)) }
            rfChannel?.let { channel ->
                item {
                    RfChannelDetails(
                        channel,
                        rfMeters[channel.id],
                        details.rfBand,
                        surfaceColor,
                        mutedColor,
                        onRfSettingsClick = { showRfSettings = true },
                    )
                }
            }
            item { Text("CHANNEL AUDIO", color = mutedColor, modifier = Modifier.padding(horizontal = 24.dp)) }
            audioChannel?.let { channel ->
                item {
                    AudioChannelDetails(channel, audioMeters[channel.id]?.rmsLevel, gainInput, { meterProgress(it, details.receiverModel) }, surfaceColor, accentColor, { gainInput -= 1; actions.updateAudioGain(channel.id, gainInput) }, { gainInput += 1; actions.updateAudioGain(channel.id, gainInput) }, onAudioSettingsClick = { showAudioSettings = true })
                }
            }
            item { Text("DEVICE CONNECTION", color = mutedColor, modifier = Modifier.padding(horizontal = 24.dp)) }
            item { DeviceConnectionInfo(surfaceColor, mutedColor) }
            item { Text(deviceNoun.uppercase(), color = mutedColor, modifier = Modifier.padding(horizontal = 24.dp)) }
            item {
                DeviceReceiverInfo(
                    receiverName = displayDeviceName,
                    receiverModel = details.receiverModel,
                    channelNumber = channelIndex + 1,
                    surfaceColor = surfaceColor,
                    mutedColor = mutedColor,
                    onAllSettingsClick = { showReceiverSettings = true },
                )
            }
        }
    }
    if (showNameEditor) {
        AlertDialog(
            onDismissRequest = { showNameEditor = false },
            title = { Text("Update device name") },
            text = { OutlinedTextField(nameInput, { nameInput = it.take(RECEIVER_ID_MAX_LENGTH) }, label = { Text("$deviceNoun ID (max $RECEIVER_ID_MAX_LENGTH characters)") }, singleLine = true) },
            confirmButton = { Button(onClick = { actions.updateDeviceName(device.id, nameInput); showNameEditor = false }, enabled = nameInput.isNotBlank() && nameInput.length <= RECEIVER_ID_MAX_LENGTH && !isUpdatingDeviceName) { Text("Update") } },
            dismissButton = { OutlinedButton(onClick = { showNameEditor = false }) { Text("Cancel") } },
        )
    }
}
