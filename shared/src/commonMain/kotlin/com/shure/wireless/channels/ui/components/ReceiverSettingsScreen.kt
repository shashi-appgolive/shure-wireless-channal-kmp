package com.shure.wireless.channels.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shure.wireless.channels.devices.domain.model.DiscoveredDevice
import com.shure.wireless.channels.devices.domain.model.RECEIVER_ID_MAX_LENGTH
import com.shure.wireless.channels.ui.theme.ShureColors

@Composable
fun ReceiverSettingsScreen(
    device: DiscoveredDevice,
    channelIndex: Int,
    onBack: () -> Unit,
    surfaceColor: Color,
    mutedColor: Color,
    isUpdatingReceiverId: Boolean,
    errorMessage: String?,
    onClearError: () -> Unit,
    onUpdateReceiverId: (String, String) -> Unit,
) {
    val deviceNoun = device.settingsDeviceNoun()
    val sections = device.receiverSettingsSections(channelIndex)
    var editing by remember(device.id) { mutableStateOf(false) }
    var draft by remember(device.id) { mutableStateOf("") }
    var pendingName by remember(device.id) { mutableStateOf<String?>(null) }
    var submissionStarted by remember(device.id) { mutableStateOf(false) }

    LaunchedEffect(device.features.name, isUpdatingReceiverId, pendingName) {
        if (pendingName != null) {
            if (isUpdatingReceiverId) submissionStarted = true
            if (device.features.name == pendingName) {
                editing = false
                pendingName = null
                submissionStarted = false
            } else if (submissionStarted && !isUpdatingReceiverId) {
                pendingName = null
                submissionStarted = false
            }
        }
    }
    Box(Modifier.fillMaxSize().background(Color.Black).windowInsetsPadding(WindowInsets.safeDrawing)) {
        Column(Modifier.widthIn(max = 640.dp).fillMaxSize().align(Alignment.TopCenter)) {
            Row(
                Modifier.fillMaxWidth().background(Color(0xFF14191B)).padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(BackArrowIcon, contentDescription = "Back to channel details", tint = ShureColors.Green)
                }
                Text("$deviceNoun Settings", Modifier.weight(1f).padding(start = 6.dp), color = Color.White, fontSize = 20.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)) {
                if (editing && errorMessage != null) {
                    item { ErrorBanner(errorMessage, onDismiss = onClearError) }
                }
                items(sections, key = { it.title }) { section ->
                    SettingsSectionTitle(section.title, mutedColor)
                    Column(Modifier.fillMaxWidth().background(surfaceColor).padding(horizontal = 20.dp)) {
                        section.rows.forEachIndexed { index, row ->
                            if (row.editable) {
                                if (editing) {
                                    val proposedName = draft.trim()
                                    val canSave = proposedName.isNotEmpty() && proposedName.length <= RECEIVER_ID_MAX_LENGTH && proposedName != row.value &&
                                        pendingName == null && !isUpdatingReceiverId
                                    val save = {
                                        if (canSave) {
                                            onClearError()
                                            pendingName = proposedName
                                            onUpdateReceiverId(device.id, proposedName)
                                        }
                                    }
                                    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                        Text("$deviceNoun ID", color = Color.White, fontSize = 16.sp)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            OutlinedTextField(
                                                value = draft,
                                                onValueChange = { draft = it.take(RECEIVER_ID_MAX_LENGTH) },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true,
                                                enabled = pendingName == null && !isUpdatingReceiverId,
                                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                                keyboardActions = KeyboardActions(onDone = { save() }),
                                                supportingText = { Text("${draft.length}/$RECEIVER_ID_MAX_LENGTH") },
                                            )
                                            if (isUpdatingReceiverId) {
                                                CircularProgressIndicator(Modifier.size(24.dp), color = ShureColors.Green)
                                            } else {
                                                IconButton(onClick = save, enabled = canSave, modifier = Modifier.semantics { contentDescription = "Save $deviceNoun ID" }) {
                                                    Text("✓", color = ShureColors.Green, fontSize = 24.sp)
                                                }
                                            }
                                            IconButton(onClick = { editing = false; pendingName = null }, enabled = pendingName == null, modifier = Modifier.semantics { contentDescription = "Cancel $deviceNoun ID edit" }) {
                                                Text("✕", color = mutedColor, fontSize = 20.sp)
                                            }
                                        }
                                    }
                                } else {
                                    Box(Modifier.fillMaxWidth().clickable(onClickLabel = "Edit $deviceNoun ID") {
                                        onClearError()
                                        draft = row.value
                                        editing = true
                                    }) {
                                        SettingsRow(row, mutedColor)
                                    }
                                }
                            } else {
                                SettingsRow(row, mutedColor)
                            }
                            if (index < section.rows.lastIndex) SettingsDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String, mutedColor: Color) {
    Text(title, Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), color = mutedColor, fontSize = 14.sp)
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(color = Color(0xFF363B3B))
}

@Composable
private fun SettingsRow(row: ReceiverSettingRow, mutedColor: Color) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(row.label, Modifier.weight(1f), color = Color.White, fontSize = 16.sp)
        Text(row.value, Modifier.weight(1f), color = if (row.highlighted) ShureColors.Green else mutedColor, fontSize = 16.sp, textAlign = TextAlign.End, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

