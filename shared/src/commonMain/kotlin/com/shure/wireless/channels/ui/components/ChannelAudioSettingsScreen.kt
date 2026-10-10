package com.shure.wireless.channels.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shure.wireless.channels.devices.domain.model.AudioChannel
import com.shure.wireless.channels.ui.theme.ShureColors

@Composable
fun ChannelAudioSettingsScreen(
    channel: AudioChannel,
    onBack: () -> Unit,
    onUpdateAudioGain: (String, Int) -> Unit,
    isUpdatingAudioGain: Boolean,
    errorMessage: String?,
    onClearError: () -> Unit,
    surfaceColor: Color,
    mutedColor: Color,
    accentColor: Color = ShureColors.Green,
) {
    val details = channel.toAudioSettingsUiModel()
    Box(Modifier.fillMaxSize().background(Color.Black).windowInsetsPadding(WindowInsets.safeDrawing)) {
        Column(Modifier.widthIn(max = 640.dp).fillMaxSize().align(Alignment.TopCenter)) {
            Row(
                Modifier.fillMaxWidth().background(Color(0xFF14191B)).padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(BackArrowIcon, contentDescription = "Back to channel details", tint = accentColor)
                }
                Text("Channel Audio Settings", Modifier.weight(1f).padding(start = 6.dp), color = Color.White, fontSize = 20.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                errorMessage?.let { message -> item { ErrorBanner(message, onDismiss = onClearError) } }
                item { AudioSettingsSectionTitle("GENERAL", mutedColor) }
                item {
                    Column(Modifier.fillMaxWidth().background(surfaceColor).padding(horizontal = 20.dp, vertical = 12.dp)) {
                        AudioGainControl(
                            gain = details.gain,
                            accentColor = accentColor,
                            onDecrease = { onUpdateAudioGain(channel.id, details.gain - 1) },
                            onIncrease = { onUpdateAudioGain(channel.id, details.gain + 1) },
                            enabled = !isUpdatingAudioGain,
                        )
                        details.audioOutput?.takeIf { it.isNotBlank() }?.let { audioOutput ->
                            HorizontalDivider(Modifier.padding(top = 12.dp), color = Color(0xFF363B3B))
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("Audio Output", Modifier.weight(1f), color = Color.White, fontSize = 18.sp)
                                Text(
                                    audioOutput,
                                    color = mutedColor,
                                    fontSize = 16.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
                if (details.toneVolume != null || details.toneFrequency != null) {
                    item { AudioSettingsSectionTitle("TONE GENERATOR", mutedColor) }
                    item {
                        Column(Modifier.fillMaxWidth().background(surfaceColor).padding(horizontal = 20.dp)) {
                            details.toneVolume?.let { volume ->
                                Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("Volume", Modifier.weight(1f), color = Color.White, fontSize = 18.sp)
                                    Text(volume, color = mutedColor, fontSize = 16.sp)
                                }
                            }
                            if (details.toneVolume != null && details.toneFrequency != null) {
                                HorizontalDivider(color = Color(0xFF363B3B))
                            }
                            details.toneFrequency?.let { frequency ->
                                val value = if (frequency % 1.0 == 0.0) frequency.toInt().toString() else frequency.toString()
                                Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("Frequency", Modifier.weight(1f), color = Color.White, fontSize = 18.sp)
                                    Text("$value Hz", color = mutedColor, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioSettingsSectionTitle(title: String, mutedColor: Color) {
    Text(title, Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), color = mutedColor, fontSize = 14.sp)
}

