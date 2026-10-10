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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shure.wireless.channels.devices.domain.model.RfChannel
import com.shure.wireless.channels.ui.theme.ShureColors

@Composable
fun ChannelRfSettingsScreen(
    channel: RfChannel,
    deviceRfBand: String?,
    onBack: () -> Unit,
    surfaceColor: Color,
    mutedColor: Color,
    accentColor: Color = ShureColors.Green,
) {
    val details = channel.toChannelRfSettingsUiModel(deviceRfBand)
    val rows = listOf(
        "Current Frequency" to details.currentFrequency,
        "Group and Channel" to details.groupAndChannel,
        "Frequency Range" to details.frequencyRange,
        "RF Band" to details.rfBand,
        "RF Profile" to details.rfProfile,
    )

    Box(Modifier.fillMaxSize().background(Color.Black).windowInsetsPadding(WindowInsets.safeDrawing)) {
        Column(Modifier.widthIn(max = 640.dp).fillMaxSize().align(Alignment.TopCenter)) {
            Row(
                Modifier.fillMaxWidth().background(Color(0xFF14191B)).padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(BackArrowIcon, contentDescription = "Back to channel details", tint = accentColor)
                }
                Text("Channel RF Settings", Modifier.weight(1f).padding(start = 6.dp), color = Color.White, fontSize = 20.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                item { RfSettingsSectionTitle("FREQUENCY", mutedColor) }
                item {
                    Column(Modifier.fillMaxWidth().background(surfaceColor).padding(horizontal = 20.dp)) {
                        rows.forEachIndexed { index, (label, value) ->
                            RfSettingsRow(label, value ?: "—", mutedColor)
                            if (index < rows.lastIndex) HorizontalDivider(color = Color(0xFF363B3B))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RfSettingsSectionTitle(title: String, mutedColor: Color) {
    Text(title, Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), color = mutedColor, fontSize = 14.sp)
}

@Composable
private fun RfSettingsRow(label: String, value: String, mutedColor: Color) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), color = Color.White, fontSize = 16.sp)
        Text(value, Modifier.weight(1f), color = mutedColor, fontSize = 16.sp, textAlign = TextAlign.End, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

